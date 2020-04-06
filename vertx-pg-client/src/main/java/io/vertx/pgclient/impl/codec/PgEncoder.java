/*
 * Copyright (C) 2018 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.vertx.pgclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.vertx.sqlclient.Tuple;
import io.vertx.pgclient.impl.util.Util;
import io.vertx.sqlclient.impl.ParamDesc;
import io.vertx.sqlclient.impl.RowDesc;
import io.vertx.sqlclient.impl.StringLongSequence;
import io.vertx.sqlclient.impl.command.CloseConnectionCommand;
import io.vertx.sqlclient.impl.command.CloseCursorCommand;
import io.vertx.sqlclient.impl.command.CloseStatementCommand;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.impl.command.ExtendedBatchQueryCommand;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;
import io.vertx.sqlclient.impl.command.InitCommand;
import io.vertx.sqlclient.impl.command.PrepareStatementCommand;
import io.vertx.sqlclient.impl.command.SimpleQueryCommand;

import java.util.ArrayDeque;
import java.util.Map;

import static io.vertx.pgclient.impl.util.Util.writeCString;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
final class PgEncoder extends ChannelOutboundHandlerAdapter {

  // Frontend message types for {@link io.reactiverse.pgclient.impl.codec.encoder.MessageEncoder}

  private static final byte PASSWORD_MESSAGE = 'p';
  private static final byte QUERY = 'Q';
  private static final byte TERMINATE = 'X';
  private static final byte PARSE = 'P';
  private static final byte BIND = 'B';
  private static final byte DESCRIBE = 'D';
  private static final byte EXECUTE = 'E';
  private static final byte CLOSE = 'C';
  private static final byte SYNC = 'S';

  private final ArrayDeque<PgCommandCodec<?, ?>> inflight;
  private ChannelHandlerContext ctx;
  private ByteBuf out;
  private PgDecoder dec;
  private final StringLongSequence psSeq = new StringLongSequence(); // used for generating named prepared statement name

  PgEncoder(PgDecoder dec, ArrayDeque<PgCommandCodec<?, ?>> inflight) {
    this.inflight = inflight;
    this.dec = dec;
  }

  void write(CommandBase<?> cmd) {
    PgCommandCodec<?, ?> codec = wrap(cmd);
    codec.completionHandler = resp -> {
      PgCommandCodec<?, ?> c = inflight.poll();
      resp.cmd = (CommandBase) c.cmd;
      ctx.fireChannelRead(resp);
    };
    codec.noticeHandler = ctx::fireChannelRead;
    inflight.add(codec);
    codec.encode(this);
  }

  private PgCommandCodec<?, ?> wrap(CommandBase<?> cmd) {
    if (cmd instanceof InitCommand) {
      return new InitCommandCodec((InitCommand) cmd);
    } else if (cmd instanceof SimpleQueryCommand<?>) {
      return new SimpleQueryCodec<>((SimpleQueryCommand<?>) cmd);
    } else if (cmd instanceof ExtendedQueryCommand<?>) {
      return new ExtendedQueryCommandCodec<>((ExtendedQueryCommand<?>) cmd);
    } else if (cmd instanceof ExtendedBatchQueryCommand<?>) {
      return new ExtendedBatchQueryCommandCodec<>((ExtendedBatchQueryCommand<?>) cmd);
    } else if (cmd instanceof PrepareStatementCommand) {
      return new PrepareStatementCommandCodec((PrepareStatementCommand) cmd);
    } else if (cmd instanceof CloseConnectionCommand) {
      return CloseConnectionCommandCodec.INSTANCE;
    } else if (cmd instanceof CloseCursorCommand) {
      return new ClosePortalCommandCodec((CloseCursorCommand) cmd);
    } else if (cmd instanceof CloseStatementCommand) {
      return new CloseStatementCommandCodec((CloseStatementCommand) cmd);
    }
    throw new AssertionError();
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    this.ctx = ctx;
  }

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    if (msg instanceof CommandBase<?>) {
      CommandBase<?> cmd = (CommandBase<?>) msg;
      write(cmd);
    } else {
      super.write(ctx, msg, promise);
    }
  }

  @Override
  public void flush(ChannelHandlerContext ctx) throws Exception {
    flush();
  }

  void flush() {
    if (out != null) {
      ByteBuf buff = out;
      out = null;
      ctx.writeAndFlush(buff);
    } else {
      ctx.flush();
    }
  }

  /**
   * This message immediately closes the connection. On receipt of this message,
   * the backend closes the connection and terminates.
   */
  void writeTerminate() {
    ensureBuffer();
    out.writeByte(TERMINATE);
    out.writeInt(4);
  }

  /**
   * <p>
   * The purpose of this message is to provide a resynchronization point for error recovery.
   * When an error is detected while processing any extended-query message, the backend issues {@link ErrorResponse},
   * then reads and discards messages until this message is reached, then issues {@link ReadyForQuery} and returns to normal
   * message processing.
   * <p>
   * Note that no skipping occurs if an error is detected while processing this message which ensures that there is one
   * and only one {@link ReadyForQuery} sent for each of this message.
   * <p>
   * Note this message does not cause a transaction block opened with BEGIN to be closed. It is possible to detect this
   * situation in {@link ReadyForQuery#txStatus()} that includes {@link TxStatus} information.
   */
  void writeSync() {
    ensureBuffer();
    out.writeByte(SYNC);
    out.writeInt(4);
  }

  /**
   * <p>
   * The message closes an existing prepared statement or portal and releases resources.
   * Note that closing a prepared statement implicitly closes any open portals that were constructed from that statement.
   * <p>
   * The response is either {@link CloseComplete} or {@link ErrorResponse}
   *
   * @param portal
   */
  void writeClosePortal(String portal) {
    ensureBuffer();
    int pos = out.writerIndex();
    out.writeByte(CLOSE);
    out.writeInt(0);
    out.writeByte('P'); // 'S' to close a prepared statement or 'P' to close a portal
    Util.writeCStringUTF8(out, portal);
    out.setInt(pos + 1, out.writerIndex() - pos - 1);
  }

  void writeClosePreparedStatement(long statementName) {
    ensureBuffer();
    int pos = out.writerIndex();
    out.writeByte(CLOSE);
    out.writeInt(0);
    out.writeByte('S'); // 'S' to close a prepared statement or 'P' to close a portal
    if (statementName == 0) {
      out.writeByte(0);
    } else {
      out.writeLong(statementName);
    }
    out.setInt(pos + 1, out.writerIndex() - pos - 1);
  }

  void writeStartupMessage(StartupMessage msg) {
    ensureBuffer();

    int pos = out.writerIndex();

    out.writeInt(0);
    // protocol version
    out.writeShort(3);
    out.writeShort(0);

    writeCString(out, StartupMessage.BUFF_USER);
    Util.writeCStringUTF8(out, msg.username);
    writeCString(out, StartupMessage.BUFF_DATABASE);
    Util.writeCStringUTF8(out, msg.database);
    for (Map.Entry<String, String> property : msg.properties.entrySet()) {
      writeCString(out, property.getKey(), UTF_8);
      writeCString(out, property.getValue(), UTF_8);
    }

    out.writeByte(0);
    out.setInt(pos, out.writerIndex() - pos);
  }

  void writePasswordMessage(PasswordMessage msg) {
    ensureBuffer();
    int pos = out.writerIndex();
    out.writeByte(PASSWORD_MESSAGE);
    out.writeInt(0);
    Util.writeCStringUTF8(out, msg.hash);
    out.setInt(pos + 1, out.writerIndex() - pos- 1);
  }

  void writeScramClientInitialMessage(ScramClientInitialMessage msg) {
    ensureBuffer();
    out.writeByte(PASSWORD_MESSAGE);
    int totalLengthPosition = out.writerIndex();
    out.writeInt(0); // message length -> will be set later

    Util.writeCStringUTF8(out, msg.mecanism);
    int msgPosition = out.writerIndex();
    out.writeInt(0);
    out.writeCharSequence(msg.message, UTF_8);

    // rewind to set the message and total length
    out.setInt(msgPosition, out.writerIndex() - msgPosition - Integer.BYTES);
    out.setInt(totalLengthPosition, out.writerIndex() - totalLengthPosition);
  }

  void writeScramClientFinalMessage(ScramClientFinalMessage msg) {
    ensureBuffer();
    out.writeByte(PASSWORD_MESSAGE);
    int totalLengthPosition = out.writerIndex();
    out.writeInt(0); // message length -> will be set later
    out.writeCharSequence(msg.message, UTF_8);

    // rewind to set the message length
    out.setInt(totalLengthPosition, out.writerIndex() - totalLengthPosition);
  }

  /**
   * <p>
   * This message includes an SQL command (or commands) expressed as a text string.
   * <p>
   * The possible response messages from the backend are
   * {@link CommandComplete}, {@link RowDesc}, {@link DataRow}, {@link EmptyQueryResponse}, {@link ErrorResponse},
   * {@link ReadyForQuery} and {@link NoticeResponse}
   */
  void writeQuery(Query query) {
    ensureBuffer();
    int pos = out.writerIndex();
    out.writeByte(QUERY);
    out.writeInt(0);
    Util.writeCStringUTF8(out, query.sql);
    out.setInt(pos + 1, out.writerIndex() - pos - 1);
  }

  /**
   * <p>
   * The message that using "statement" variant specifies the name of an existing prepared statement.
   * <p>
   * The response is a {@link ParamDesc} message describing the parameters needed by the statement,
   * followed by a {@link RowDesc} message describing the rows that will be returned when the statement is eventually
   * executed or a {@link NoData} message if the statement will not return rows.
   * {@link ErrorResponse} is issued if there is no such prepared statement.
   * <p>
   * Note that since {@link Bind} has not yet been issued, the formats to be used for returned columns are not yet known to
   * the backend; the format code fields in the {@link RowDesc} message will be zeroes in this case.
   * <p>
   * The message that using "portal" variant specifies the name of an existing portal.
   * <p>
   * The response is a {@link RowDesc} message describing the rows that will be returned by executing the portal;
   * or a {@link NoData} message if the portal does not contain a query that will return rows; or {@link ErrorResponse}
   * if there is no such portal.
   */
  void writeDescribe(Describe describe) {
    ensureBuffer();
    int pos = out.writerIndex();
    out.writeByte(DESCRIBE);
    out.writeInt(0);
    if (describe.statement != 0) {
      out.writeByte('S');
      out.writeLong(describe.statement);
    } else if (describe.portal != null) {
      out.writeByte('P');
      Util.writeCStringUTF8(out, describe.portal);
    } else {
      out.writeByte('S');
      Util.writeCStringUTF8(out, "");
    }
    out.setInt(pos + 1, out.writerIndex() - pos- 1);
  }

  /**
   * <p>
   * The message contains a textual SQL query string.
   * <p>
   * The response is either {@link ParseComplete} or {@link ErrorResponse}
   */
  void writeParse(Parse parse) {
    ensureBuffer();
    int pos = out.writerIndex();
    out.writeByte(PARSE);
    out.writeInt(0);
    if (parse.statement == 0) {
      out.writeByte(0);
    } else {
      out.writeLong(parse.statement);
    }
    Util.writeCStringUTF8(out, parse.query);
    // no parameter data types (OIDs)
    // if(paramDataTypes == null) {
    out.writeShort(0);
    // } else {
    //   // Parameter data types (OIDs)
    //   out.writeShort(paramDataTypes.length);
    //   for (int paramDataType : paramDataTypes) {
    //     out.writeInt(paramDataType);
    //   }
    // }
    out.setInt(pos + 1, out.writerIndex() - pos - 1);
  }

  /**
   * The message specifies the portal and a maximum row count (zero meaning "fetch all rows") of the result.
   * <p>
   * The row count of the result is only meaningful for portals containing commands that return row sets;
   * in other cases the command is always executed to completion, and the row count of the result is ignored.
   * <p>
   * The possible responses to this message are the same as {@link Query} message, except that
   * it doesn't cause {@link ReadyForQuery} or {@link RowDesc} to be issued.
   * <p>
   * If Execute terminates before completing the execution of a portal, it will send a {@link PortalSuspended} message;
   * the appearance of this message tells the frontend that another Execute should be issued against the same portal to
   * complete the operation. The {@link CommandComplete} message indicating completion of the source SQL command
   * is not sent until the portal's execution is completed. Therefore, This message is always terminated by
   * the appearance of exactly one of these messages: {@link CommandComplete},
   * {@link EmptyQueryResponse}, {@link ErrorResponse} or {@link PortalSuspended}.
   *
   * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
   */
  void writeExecute(String portal, int rowCount) {
    ensureBuffer();
    int pos = out.writerIndex();
    out.writeByte(EXECUTE);
    out.writeInt(0);
    if (portal != null) {
      out.writeCharSequence(portal, UTF_8);
    }
    out.writeByte(0);
    out.writeInt(rowCount); // Zero denotes "no limit" maybe for ReadStream<Row>
    out.setInt(pos + 1, out.writerIndex() - pos - 1);
  }

  /**
   * <p>
   * The message gives the name of the prepared statement, the name of portal,
   * and the values to use for any parameter values present in the prepared statement.
   * The supplied parameter set must match those needed by the prepared statement.
   * <p>
   * The response is either {@link BindComplete} or {@link ErrorResponse}.
   */
  void writeBind(Bind bind, String portal, Tuple paramValues) {
    ensureBuffer();
    int pos = out.writerIndex();
    out.writeByte(BIND);
    out.writeInt(0);
    if (portal != null) {
      out.writeCharSequence(portal, UTF_8);
    }
    out.writeByte(0);
    if (bind.statement == 0) {
      out.writeByte(0);
    } else {
      out.writeLong(bind.statement);
    }
    int paramLen = paramValues.size();
    out.writeShort(paramLen);
    // Parameter formats
    for (int c = 0;c < paramLen;c++) {
      // for now each format is Binary
      out.writeShort(bind.paramTypes[c].supportsBinary ? 1 : 0);
    }
    out.writeShort(paramLen);
    for (int c = 0;c < paramLen;c++) {
      Object param = paramValues.getValue(c);
      if (param == null) {
        // NULL value
        out.writeInt(-1);
      } else {
        DataType dataType = bind.paramTypes[c];
        if (dataType.supportsBinary) {
          int idx = out.writerIndex();
          out.writeInt(0);
          DataTypeCodec.encodeBinary(dataType, param, out);
          out.setInt(idx, out.writerIndex() - idx - 4);
        } else {
          DataTypeCodec.encodeText(dataType, param, out);
        }
      }
    }

    // MAKE resultColumsn non null to avoid null check

    // Result columns are all in Binary format
    if (bind.resultColumns.length > 0) {
      out.writeShort(bind.resultColumns.length);
      for (PgColumnDesc resultColumn : bind.resultColumns) {
        out.writeShort(resultColumn.dataType.supportsBinary ? 1 : 0);
      }
    } else {
      out.writeShort(1);
      out.writeShort(1);
    }
    out.setInt(pos + 1, out.writerIndex() - pos - 1);
  }

  long nextStatementName() {
    return psSeq.next();
  }

  private void ensureBuffer() {
    if (out == null) {
      out = ctx.alloc().ioBuffer();
    }
  }
}

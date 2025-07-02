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
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.SocketChannel;
import io.vertx.sqlclient.Tuple;
import io.vertx.pgclient.impl.util.Util;
import io.vertx.sqlclient.impl.HexSequence;
import io.vertx.sqlclient.internal.RowDescriptorBase;
import io.vertx.sqlclient.spi.protocol.CloseConnectionCommand;

import java.util.*;

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

  private final PgCodec codec;
  final boolean useLayer7Proxy;
  private ChannelHandlerContext ctx;
  private final HexSequence psSeq = new HexSequence(); // used for generating named prepared statement name
  boolean closeSent;

  private ArrayList<Object> pendingMessages = new ArrayList<>();
  private int capacityEstimate = 0;

  PgEncoder(boolean useLayer7Proxy, PgCodec codec) {
    this.useLayer7Proxy = useLayer7Proxy;
    this.codec = codec;
  }

  private void enqueueMessage(Object msg, int estimate) {
    pendingMessages.add(msg);
    capacityEstimate += estimate;
  }

  private void enqueueMessage(Object msg, Object p1, int estimate) {
    pendingMessages.add(msg);
    pendingMessages.add(p1);
    capacityEstimate += estimate;
  }

  private void enqueueMessage(Object msg, Object p1, Object p2, int estimate) {
    pendingMessages.add(msg);
    pendingMessages.add(p1);
    pendingMessages.add(p2);
    capacityEstimate += estimate;
  }

  private void enqueueMessage(Object msg, Object p1, Object p2, Object p3, int estimate) {
    pendingMessages.add(msg);
    pendingMessages.add(p1);
    pendingMessages.add(p2);
    pendingMessages.add(p3);
    capacityEstimate += estimate;
  }

  @Override
  public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    if (!closeSent) {
      CloseConnectionCommand cmd = CloseConnectionCommand.INSTANCE;
      PgCommandCodec<?, ?> codec = PgCommandCodec.wrap(cmd);
      codec.encode(this);
    }
  }

  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    pendingMessages.clear();
    capacityEstimate = 0;
  }

  void write(PgCommandCodec<?, ?> cmd) {
    if (codec.add(cmd)) {
      cmd.encode(this);
    }
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    this.ctx = ctx;
  }

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    if (msg instanceof PgCommandCodec<?, ?>) {
      write((PgCommandCodec<?, ?>) msg);
    } else {
      super.write(ctx, msg, promise);
    }
  }

  @Override
  public void flush(ChannelHandlerContext ctx) {
    flush();
  }

  private ByteBuf renderPendingMessages() {
    if (pendingMessages.isEmpty()) {
      return Unpooled.EMPTY_BUFFER;
    }

    ByteBuf out = ctx.alloc().ioBuffer(capacityEstimate);
    int index = 0;
    int size = pendingMessages.size();
    while (index < size) {
      Object msg = pendingMessages.get(index++);
      if (msg.getClass() == SyncMessage.class) {
        renderSync(out);
      } else if (msg.getClass() == TerminateMessage.class) {
        renderTerminate(out);
      } else if (msg.getClass() == ClosePortalMessage.class) {
        String portal = (String) pendingMessages.get(index++);
        renderClosePortal(portal, out);
      } else if (msg.getClass() == ClosePreparedStatementMessage.class) {
        byte[] statementName = (byte[]) pendingMessages.get(index++);
        renderClosePreparedStatement(statementName, out);
      } else if (msg.getClass() == StartupMessage.class) {
        renderStartupMessage((StartupMessage) msg, out);
      } else if (msg.getClass() == PasswordMessage.class) {
        renderPasswordMessage((PasswordMessage) msg, out);
      } else if (msg.getClass() == ScramClientInitialMessage.class) {
        renderScramInitialMessage((ScramClientInitialMessage) msg, out);
      } else if (msg.getClass() == ScramClientFinalMessage.class) {
        renderScramFinalMessage((ScramClientFinalMessage) msg, out);
      } else if (msg.getClass() == QueryMessage.class) {
        renderQueryMessage((QueryMessage) msg, out);
      } else if (msg.getClass() == DescribeMessage.class) {
        renderDescribe((DescribeMessage) msg, out);
      } else if (msg.getClass() == ParseMessage.class) {
        String sql = (String) pendingMessages.get(index++);
        byte[] statement = (byte[]) pendingMessages.get(index++);
        DataType[] parameterTypes = (DataType[]) pendingMessages.get(index++);
        renderParse(sql, statement, parameterTypes, out);
      } else if (msg.getClass() == ExecuteMessage.class) {
        String portal = (String) pendingMessages.get(index++);
        int rowCount = (Integer) pendingMessages.get(index++);
        renderExecute(portal, rowCount, out);
      } else if (msg.getClass() == BindMessage.class) {
        String portal = (String) pendingMessages.get(index++);
        Tuple paramValues = (Tuple) pendingMessages.get(index++);
        renderBind((BindMessage) msg, portal, paramValues, out);
      } else {
        throw new AssertionError();
      }
    }
    pendingMessages.clear();
    capacityEstimate = 0;
    return out;
  }

  private static void renderQueryMessage(QueryMessage query, ByteBuf out) {
    int pos = out.writerIndex();
    out.writeByte(QUERY);
    out.writeInt(0);
    Util.writeCStringUTF8(out, query.sql);
    out.setInt(pos + 1, out.writerIndex() - pos - 1);
  }

  private static int estimateQueryMessage(QueryMessage query) {
    return 1 + 4 + DataTypeEstimator.estimateCStringUTF8(query.sql);
  }

  private static  void renderPasswordMessage(PasswordMessage msg, ByteBuf out) {
    int pos = out.writerIndex();
    out.writeByte(PASSWORD_MESSAGE);
    out.writeInt(0);
    Util.writeCStringUTF8(out, msg.hash);
    out.setInt(pos + 1, out.writerIndex() - pos- 1);
  }

  private static  int estimatePasswordMessage(PasswordMessage msg) {
    return 1 + 4 + DataTypeEstimator.estimateCStringUTF8(msg.hash);
  }

  private static  void renderStartupMessage(StartupMessage msg, ByteBuf out) {
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

  private static  int estimateStartupMessage(StartupMessage msg) {
    int length = 4 +
        2 +
        2 +
      (StartupMessage.BUFF_USER_LENGTH + 1) +
      DataTypeEstimator.estimateCStringUTF8(msg.username) +
      (StartupMessage.BUFF_DATABASE_LENGTH + 1) +
      DataTypeEstimator.estimateCStringUTF8(msg.database);
    for (Map.Entry<String, String> property : msg.properties.entrySet()) {
      length += DataTypeEstimator.estimateCStringUTF8(property.getKey());
      length += DataTypeEstimator.estimateCStringUTF8(property.getValue());
    }
    length++;
    return length;
  }

  private static  void renderScramInitialMessage(ScramClientInitialMessage msg, ByteBuf out) {
    out.writeByte(PASSWORD_MESSAGE);
    int totalLengthPosition = out.writerIndex();
    out.writeInt(0); // message length -> will be set later

    Util.writeCStringUTF8(out, msg.mechanism);
    int msgPosition = out.writerIndex();
    out.writeInt(0);
    out.writeCharSequence(msg.message, UTF_8);

    // rewind to set the message and total length
    out.setInt(msgPosition, out.writerIndex() - msgPosition - Integer.BYTES);
    out.setInt(totalLengthPosition, out.writerIndex() - totalLengthPosition);
  }

  private static  int estimateScramInitialMessage(ScramClientInitialMessage msg) {
    return 1 + 4 + DataTypeEstimator.estimateCStringUTF8(msg.mechanism) + 4 + DataTypeEstimator.estimateUTF8(msg.message);
  }

  private static  void renderScramFinalMessage(ScramClientFinalMessage msg, ByteBuf out) {
    out.writeByte(PASSWORD_MESSAGE);
    int totalLengthPosition = out.writerIndex();
    out.writeInt(0); // message length -> will be set later
    out.writeCharSequence(msg.message, UTF_8);

    // rewind to set the message length
    out.setInt(totalLengthPosition, out.writerIndex() - totalLengthPosition);
  }

  private static  int estimateScramFinalMessage(ScramClientFinalMessage msg) {
    return 1 + 4 + DataTypeEstimator.estimateUTF8(msg.message);
  }

  private static  void renderClosePreparedStatement(byte[] statementName, ByteBuf out) {
    int pos = out.writerIndex();
    out.writeByte(CLOSE);
    out.writeInt(0);
    out.writeByte('S'); // 'S' to close a prepared statement or 'P' to close a portal
    out.writeBytes(statementName);
    out.setInt(pos + 1, out.writerIndex() - pos - 1);
  }

  private static  int estimateClosePreparedStatement(byte[] statementName) {
    return 1 + 4 + 1 + DataTypeEstimator.estimateByteArray(statementName);
  }

  private static  void renderTerminate(ByteBuf out) {
    out.writeByte(TERMINATE);
    out.writeInt(4);
  }

  private static  int estimateTerminate() {
    return 1 + 4;
  }

  private static  void renderSync(ByteBuf out) {
    out.writeByte(SYNC);
    out.writeInt(4);
  }

  private static  int estimateSync() {
    return 1 + 4;
  }

  private static void renderExecute(String portal, int rowCount, ByteBuf out) {
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

  private static int estimateExecute(String portal, int rowCount) {
    return 1 + 4 + (portal != null ? DataTypeEstimator.estimateUTF8(portal) : 0) + 1 + 4;
  }

  private static  void renderDescribe(DescribeMessage describe, ByteBuf out) {
    int pos = out.writerIndex();
    out.writeByte(DESCRIBE);
    out.writeInt(0);
    if (describe.statement.length > 1) {
      out.writeByte('S');
      out.writeBytes(describe.statement);
    } else if (describe.portal != null) {
      out.writeByte('P');
      Util.writeCStringUTF8(out, describe.portal);
    } else {
      out.writeByte('S');
      Util.writeCStringUTF8(out, "");
    }
    out.setInt(pos + 1, out.writerIndex() - pos- 1);
  }

  private static  int estimateDescribe(DescribeMessage describe) {
    int length = 1 + 4;
    if (describe.statement.length > 1) {
      length += 1 + DataTypeEstimator.estimateByteArray(describe.statement);
    } else if (describe.portal != null) {
      length += 1 + DataTypeEstimator.estimateCStringUTF8(describe.portal);
    } else {
      length += 1 + DataTypeEstimator.estimateCStringUTF8("");
    }
    return length;
  }

  private static void renderParse(String sql, byte[] statement, DataType[] parameterTypes, ByteBuf out) {
    int pos = out.writerIndex();
    out.writeByte(PARSE);
    out.writeInt(0);
    out.writeBytes(statement);
    Util.writeCStringUTF8(out, sql);
    if (parameterTypes == null) {
      // Let pg figure out
      out.writeShort(0);
    } else {
      out.writeShort(parameterTypes.length);
      for (DataType parameterType : parameterTypes) {
        out.writeInt(parameterType.id);
      }
    }
    out.setInt(pos + 1, out.writerIndex() - pos - 1);
  }

  private static int estimateParse(String sql, byte[] statement, DataType[] parameterTypes) {
    return 1 +
      4 +
      DataTypeEstimator.estimateByteArray(statement) +
      DataTypeEstimator.estimateCStringUTF8(sql) +
      2 + (parameterTypes == null ? 0 : parameterTypes.length * 4);
  }

  private static void renderBind(BindMessage bind, String portal, Tuple paramValues, ByteBuf out) {
    int pos = out.writerIndex();
    out.writeByte(BIND);
    out.writeInt(0);
    if (portal != null) {
      out.writeCharSequence(portal, UTF_8);
    }
    out.writeByte(0);
    out.writeBytes(bind.statement);
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

  private static int estimateBind(BindMessage bind, String portal, Tuple paramValues) {

    int paramLen = paramValues.size();

    int length = 1 +
      4 +
      (portal != null ? DataTypeEstimator.estimateUTF8(portal) : 0) +
      1 +
      DataTypeEstimator.estimateByteArray(bind.statement) +
      2 +
      paramLen * 2 +
      2;

    for (int c = 0;c < paramLen;c++) {
      Object param = paramValues.getValue(c);
      if (param == null) {
        length += 4;
      } else {
        DataType dataType = bind.paramTypes[c];
        length += 4;
        if (dataType.supportsBinary) {
          int estimator = dataType.lengthEstimator;
          if (dataType.array) {
            length += 4 + 4 + 4 + 4 + 4;
            Object[] array = (Object[]) param;
            for (Object elt : array) {
              length += 4;
              if (elt != null) {
                length += DataTypeEstimator.estimate(estimator, elt);
              }
            }
          } else {
            length += DataTypeEstimator.estimate(estimator, param);
          }
        } else {
          length += DataTypeEstimator.estimate(dataType.lengthEstimator, param);
        }
      }
    }

    // Result columns are all in Binary format
    if (bind.resultColumns.length > 0) {
      length += 2 + bind.resultColumns.length * 2;
    } else {
      length += 2 + 2;
    }
    return length;
  }

  private static void renderClosePortal(String portal, ByteBuf out) {
      int pos = out.writerIndex();
      out.writeByte(CLOSE);
      out.writeInt(0);
      out.writeByte('P'); // 'S' to close a prepared statement or 'P' to close a portal
      Util.writeCStringUTF8(out, portal);
      out.setInt(pos + 1, out.writerIndex() - pos - 1);
  }

  private static  int estimateClosePortal(String portal) {
    return 1 + 4 + 1 + DataTypeEstimator.estimateCStringUTF8(portal);
  }

  void close() {
    ByteBuf buff = renderPendingMessages();
    ctx.writeAndFlush(buff).addListener(v -> {
      Channel ch = channelHandlerContext().channel();
      if (ch instanceof SocketChannel) {
        SocketChannel channel = (SocketChannel) ch;
        channel.shutdownOutput();
      }
    });
  }

  void flush() {
    ByteBuf buff = renderPendingMessages();
    if (buff == Unpooled.EMPTY_BUFFER) {
      ctx.flush();
    } else {
      ctx.writeAndFlush(buff, ctx.voidPromise());
    }
  }

  /**
   * This message immediately closes the connection. On receipt of this message,
   * the backend closes the connection and terminates.
   */
  void writeTerminate() {
    enqueueMessage(TerminateMessage.INSTANCE, estimateTerminate());
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
    enqueueMessage(SyncMessage.INSTANCE, estimateSync());
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
    enqueueMessage(ClosePortalMessage.INSTANCE, portal, estimateClosePortal(portal));
  }

  void writeClosePreparedStatement(byte[] statementName) {
    enqueueMessage(ClosePreparedStatementMessage.INSTANCE, statementName, estimateClosePreparedStatement(statementName));
  }

  void writeStartupMessage(StartupMessage msg) {
    enqueueMessage(msg, estimateStartupMessage(msg));
  }

  void writePasswordMessage(PasswordMessage msg) {
    enqueueMessage(msg, estimatePasswordMessage(msg));
  }

  void writeScramClientInitialMessage(ScramClientInitialMessage msg) {
    enqueueMessage(msg, estimateScramInitialMessage(msg));
  }

  void writeScramClientFinalMessage(ScramClientFinalMessage msg) {
    enqueueMessage(msg, estimateScramFinalMessage(msg));
  }

  /**
   * <p>
   * This message includes an SQL command (or commands) expressed as a text string.
   * <p>
   * The possible response messages from the backend are
   * {@link CommandComplete}, {@link RowDescriptorBase}, {@link DataRow}, {@link EmptyQueryResponse}, {@link ErrorResponse},
   * {@link ReadyForQuery} and {@link NoticeResponse}
   */
  void writeQuery(QueryMessage query) {
    enqueueMessage(query, estimateQueryMessage(query));
  }

  /**
   * <p>
   * The message that using "statement" variant specifies the name of an existing prepared statement.
   * <p>
   * The response is a {@link ParamDesc} message describing the parameters needed by the statement,
   * followed by a {@link RowDescriptorBase} message describing the rows that will be returned when the statement is eventually
   * executed or a {@link NoData} message if the statement will not return rows.
   * {@link ErrorResponse} is issued if there is no such prepared statement.
   * <p>
   * Note that since {@link BindMessage} has not yet been issued, the formats to be used for returned columns are not yet known to
   * the backend; the format code fields in the {@link RowDescriptorBase} message will be zeroes in this case.
   * <p>
   * The message that using "portal" variant specifies the name of an existing portal.
   * <p>
   * The response is a {@link RowDescriptorBase} message describing the rows that will be returned by executing the portal;
   * or a {@link NoData} message if the portal does not contain a query that will return rows; or {@link ErrorResponse}
   * if there is no such portal.
   */
  void writeDescribe(DescribeMessage describe) {
    enqueueMessage(describe, estimateDescribe(describe));
  }

  void writeParse(String sql, byte[] statement, DataType[] parameterTypes) {
    enqueueMessage(ParseMessage.INSTANCE, sql, statement, parameterTypes, estimateParse(sql, statement, parameterTypes));
  }

  /**
   * The message specifies the portal and a maximum row count (zero meaning "fetch all rows") of the result.
   * <p>
   * The row count of the result is only meaningful for portals containing commands that return row sets;
   * in other cases the command is always executed to completion, and the row count of the result is ignored.
   * <p>
   * The possible responses to this message are the same as {@link QueryMessage} message, except that
   * it doesn't cause {@link ReadyForQuery} or {@link RowDescriptorBase} to be issued.
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
    enqueueMessage(ExecuteMessage.INSTANCE, portal, rowCount, estimateExecute(portal, rowCount));
  }

  /**
   * <p>
   * The message gives the name of the prepared statement, the name of portal,
   * and the values to use for any parameter values present in the prepared statement.
   * The supplied parameter set must match those needed by the prepared statement.
   * <p>
   * The response is either {@link BindComplete} or {@link ErrorResponse}.
   */
  void writeBind(BindMessage bind, String portal, Tuple paramValues) {
    enqueueMessage(bind, portal, paramValues, estimateBind(bind, portal, paramValues));
  }

  byte[] nextStatementName() {
    return psSeq.next();
  }

  public ChannelHandlerContext channelHandlerContext() {
    return ctx;
  }
}

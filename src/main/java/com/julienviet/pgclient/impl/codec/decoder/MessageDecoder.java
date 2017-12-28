/*
 * Copyright (C) 2017 Julien Viet
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

package com.julienviet.pgclient.impl.codec.decoder;

import com.julienviet.pgclient.PgResult;
import com.julienviet.pgclient.impl.codec.Column;
import com.julienviet.pgclient.impl.codec.DataFormat;
import com.julienviet.pgclient.impl.codec.DataType;
import com.julienviet.pgclient.impl.codec.decoder.message.*;
import com.julienviet.pgclient.impl.codec.util.Util;
import com.julienviet.pgclient.impl.PgResultImpl;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.ByteProcessor;

import java.util.Deque;
import java.util.List;

import static com.julienviet.pgclient.impl.codec.decoder.message.type.AuthenticationType.*;
import static com.julienviet.pgclient.impl.codec.decoder.message.type.ErrorOrNoticeType.*;
import static com.julienviet.pgclient.impl.codec.decoder.message.type.MessageType.*;

/**
 *
 * Decoder for <a href="https://www.postgresql.org/docs/9.5/static/protocol.html">PostgreSQL protocol</a>
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class MessageDecoder extends ByteToMessageDecoder {

  private final Deque<DecodeContext> decodeQueue;
  private RowDescription rowDesc;

  public MessageDecoder(Deque<DecodeContext> decodeQueue) {
    this.decodeQueue = decodeQueue;
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    while (true) {
      if (in.readableBytes() < 5) {
        break;
      }
      int beginIdx = in.readerIndex();
      byte id = in.getByte(beginIdx);
      int length = in.getInt(beginIdx + 1);
      int endIdx = beginIdx + length + 1;
      final int writerIndex = in.writerIndex();
      if (writerIndex < endIdx) {
        break;
      }
      try {
        in.setIndex(beginIdx + 5, endIdx);
        switch (id) {
          case READY_FOR_QUERY: {
            decodeReadyForQuery(in, out);
            break;
          }
          case DATA_ROW: {
            decodeDataRow(in);
            break;
          }
          case COMMAND_COMPLETE: {
            decodeCommandComplete(in, out);
            break;
          }
          case BIND_COMPLETE: {
            decodeBindComplete(out);
            break;
          }
          default: {
            decodeMessage(id, in ,out);
          }
        }
      } finally {
        in.setIndex(endIdx, writerIndex);
      }
    }
  }

  private void decodeMessage(byte id, ByteBuf in, List<Object> out) {
    switch (id) {
      case ROW_DESCRIPTION: {
        decodeRowDescription(in, out);
        break;
      }
      case ERROR_RESPONSE: {
        decodeError(in, out);
        break;
      }
      case NOTICE_RESPONSE: {
        decodeNotice(in, out);
        break;
      }
      case AUTHENTICATION: {
        decodeAuthentication(in, out);
        break;
      }
      case EMPTY_QUERY_RESPONSE: {
        decodeEmptyQueryResponse(out);
        break;
      }
      case PARSE_COMPLETE: {
        decodeParseComplete(out);
        break;
      }
      case CLOSE_COMPLETE: {
        decodeCloseComplete(out);
        break;
      }
      case NO_DATA: {
        decodeNoData(out);
        break;
      }
      case PORTAL_SUSPENDED: {
        decodePortalSuspended(out);
        break;
      }
      case PARAMETER_DESCRIPTION: {
        decodeParameterDescription(in, out);
        break;
      }
      case PARAMETER_STATUS: {
        decodeParameterStatus(in, out);
        break;
      }
      case BACKEND_KEY_DATA: {
        decodeBackendKeyData(in, out);
        break;
      }
      case NOTIFICATION_RESPONSE: {
        decodeNotificationResponse(in, out);
        break;
      }
      case FUNCTION_RESULT: {
        decodeFunctionResult(in, out);
        break;
      }
      default: {
        throw new UnsupportedOperationException("Not implemented " + id);
      }
    }
  }

  private void decodePortalSuspended(List<Object> out) {
    DecodeContext ctx = decodeQueue.peek();
    ctx.current = null;
    PgResult result = ctx.resultDecoder.complete(0);
    out.add(new PortalSuspended(result));
  }

  private void decodeCommandComplete(ByteBuf in, List<Object> out) {
    DecodeContext ctx = decodeQueue.peek();
    ctx.current = null;
    int updated = decodeCommandComplete(in);
    CommandComplete complete;
    if (ctx.resultDecoder == null) {
      complete = new CommandComplete(new PgResultImpl(updated));
    } else {
      complete = new CommandComplete(ctx.resultDecoder.complete(updated));
    }
    out.add(complete);
  }

  private void decodeDataRow(ByteBuf in) {
    DecodeContext decodeCtx = decodeQueue.peek();
    RowDescription desc = decodeCtx.current;
    if (desc == null) {
      desc = decodeCtx.peekDesc ? rowDesc : decodeCtx.rowDesc;
      decodeCtx.current = desc;
      decodeCtx.resultDecoder.init(decodeCtx.current);
    }
    int len = in.readUnsignedShort();
    decodeCtx.resultDecoder.decodeRow(len, in);
  }

  private void decodeFunctionResult(ByteBuf in, List<Object> out) {
    int len = (int) in.readUnsignedInt();
    DecodeContext ctx = decodeQueue.peek();
    Object value = ctx.returnType.binaryDecoder.decode(len, in);
    out.add(new FunctionCallResponse(value));
  }

  private void decodeRowDescription(ByteBuf in, List<Object> out) {
    Column[] columns = decodeRowDescription(in);
    rowDesc = new RowDescription(columns);
    out.add(rowDesc);
  }

  private void decodeReadyForQuery(ByteBuf in, List<Object> out) {
    out.add(ReadyForQuery.decode(in.readByte()));
    decodeQueue.poll();
  }

  private void decodeError(ByteBuf in, List<Object> out) {
    decodeErrorOrNotice(ErrorResponse.INSTANCE, in, out);
  }

  private void decodeNotice(ByteBuf in, List<Object> out) {
    decodeErrorOrNotice(NoticeResponse.INSTANCE, in, out);
  }

  private void decodeErrorOrNotice(Response response, ByteBuf in, List<Object> out) {

    byte type;

    while ((type = in.readByte()) != 0) {

      switch (type) {

        case SEVERITY:
          response.setSeverity(Util.readCStringUTF8(in));
          break;

        case CODE:
          response.setCode(Util.readCStringUTF8(in));
          break;

        case MESSAGE:
          response.setMessage(Util.readCStringUTF8(in));
          break;

        case DETAIL:
          response.setDetail(Util.readCStringUTF8(in));
          break;

        case HINT:
          response.setHint(Util.readCStringUTF8(in));
          break;

        case INTERNAL_POSITION:
          response.setInternalPosition(Util.readCStringUTF8(in));
          break;

        case INTERNAL_QUERY:
          response.setInternalQuery(Util.readCStringUTF8(in));
          break;

        case POSITION:
          response.setPosition(Util.readCStringUTF8(in));
          break;

        case WHERE:
          response.setWhere(Util.readCStringUTF8(in));
          break;

        case FILE:
          response.setFile(Util.readCStringUTF8(in));
          break;

        case LINE:
          response.setLine(Util.readCStringUTF8(in));
          break;

        case ROUTINE:
          response.setRoutine(Util.readCStringUTF8(in));
          break;

        case SCHEMA:
          response.setSchema(Util.readCStringUTF8(in));
          break;

        case TABLE:
          response.setTable(Util.readCStringUTF8(in));
          break;

        case COLUMN:
          response.setColumn(Util.readCStringUTF8(in));
          break;

        case DATA_TYPE:
          response.setDataType(Util.readCStringUTF8(in));
          break;

        case CONSTRAINT:
          response.setConstraint(Util.readCStringUTF8(in));
          break;

        default:
          Util.readCStringUTF8(in);
          break;
      }
    }
    out.add(response);
  }

  private void decodeAuthentication(ByteBuf in, List<Object> out) {

    int type = in.readInt();
    switch (type) {
      case OK: {
        out.add(AuthenticationOk.INSTANCE);
      }
      break;
      case MD5_PASSWORD: {
        byte[] salt = new byte[4];
        in.readBytes(salt);
        out.add(new AuthenticationMD5Password(salt));
      }
      break;
      case CLEARTEXT_PASSWORD: {
        out.add(AuthenticationClearTextPassword.INSTANCE);
      }
      break;
      case KERBEROS_V5:
      case SCM_CREDENTIAL:
      case GSS:
      case GSS_CONTINUE:
      case SSPI:
      default:
        throw new UnsupportedOperationException("Authentication type is not supported in the client");
    }
  }

  private CommandCompleteProcessor processor = new CommandCompleteProcessor();

  static class CommandCompleteProcessor implements ByteProcessor {
    private static final byte SPACE = 32;
    private int rows;
    boolean afterSpace;
    int parse(ByteBuf in) {
      afterSpace = false;
      rows = 0;
      in.forEachByte(in.readerIndex(), in.readableBytes() - 1, this);
      return rows;
    }
    @Override
    public boolean process(byte value) throws Exception {
      boolean space = value == SPACE;
      if (afterSpace) {
        if (space) {
          rows = 0;
        } else {
          rows = rows * 10 + (value - '0');
        }
      } else {
        afterSpace = space;
      }
      return true;
    }
  }

  private int decodeCommandComplete(ByteBuf in) {
    return processor.parse(in);
  }

  private Column[]  decodeRowDescription(ByteBuf in) {
    Column[] columns = new Column[in.readUnsignedShort()];
    for (int c = 0; c < columns.length; ++c) {
      String fieldName = Util.readCStringUTF8(in);
      int tableOID = in.readInt();
      short columnAttributeNumber = in.readShort();
      int typeOID = in.readInt();
      short typeSize = in.readShort();
      int typeModifier = in.readInt();
      int textOrBinary = in.readUnsignedShort(); // Useless for now
      Column column = new Column(
        fieldName,
        tableOID,
        columnAttributeNumber,
        DataType.valueOf(typeOID),
        typeSize,
        typeModifier,
        DataFormat.valueOf(textOrBinary)
      );
      columns[c] = column;
    }
    return columns;
  }

  private void decodeParseComplete(List<Object> out) {
    out.add(ParseComplete.INSTANCE);
  }

  private void decodeBindComplete(List<Object> out) {
    out.add(BindComplete.INSTANCE);
  }

  private void decodeCloseComplete(List<Object> out) {
    out.add(CloseComplete.INSTANCE);
  }

  private void decodeNoData(List<Object> out) {
    out.add(NoData.INSTANCE);
  }

  private void decodeParameterDescription(ByteBuf in, List<Object> out) {
    DataType[] paramDataTypes = new DataType[in.readUnsignedShort()];
    for (int c = 0; c < paramDataTypes.length; ++c) {
      paramDataTypes[c] = DataType.valueOf(in.readInt());
    }
    out.add(new ParameterDescription(paramDataTypes));
  }

  private void decodeParameterStatus(ByteBuf in, List<Object> out) {
    out.add(new ParameterStatus(Util.readCStringUTF8(in), Util.readCStringUTF8(in)));
  }

  private void decodeEmptyQueryResponse(List<Object> out) {
    out.add(EmptyQueryResponse.INSTANCE);
  }

  private void decodeBackendKeyData(ByteBuf in, List<Object> out) {
    out.add(new BackendKeyData(in.readInt(), in.readInt()));
  }

  private void decodeNotificationResponse(ByteBuf in, List<Object> out) {
    out.add(new NotificationResponse(in.readInt(), Util.readCStringUTF8(in), Util.readCStringUTF8(in)));
  }
}

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

package com.julienviet.pgclient.codec.decoder;

import com.julienviet.pgclient.codec.Column;
import com.julienviet.pgclient.codec.DataFormat;
import com.julienviet.pgclient.codec.DataType;
import com.julienviet.pgclient.codec.TransactionStatus;
import com.julienviet.pgclient.codec.decoder.message.*;
import com.julienviet.pgclient.codec.util.Util;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.vertx.core.json.JsonArray;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static com.julienviet.pgclient.codec.decoder.message.type.AuthenticationType.*;
import static com.julienviet.pgclient.codec.decoder.message.type.CommandCompleteType.*;
import static com.julienviet.pgclient.codec.decoder.message.type.ErrorOrNoticeType.*;
import static com.julienviet.pgclient.codec.decoder.message.type.MessageType.*;
import static java.nio.charset.StandardCharsets.*;

/**
 *
 * Decoder for <a href="https://www.postgresql.org/docs/9.5/static/protocol.html">PostgreSQL protocol</a>
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class MessageDecoder extends ByteToMessageDecoder {

  private final Deque<DecodeContext> decodeQueue;
  private RowDescription rowDesc;
  private List<JsonArray> rows = new ArrayList<>();

  public MessageDecoder(Deque<DecodeContext> decodeQueue) {
    this.decodeQueue = decodeQueue;
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    while (true) {
      if (in.readableBytes() == 1) {
        switch (in.getByte(0)) {
          case SSL_YES: {
            out.add(new SSLResponse(true));
            in.readerIndex(in.readerIndex() + 1);
            return;
          }
          case SSL_NO: {
            out.add(new SSLResponse(false));
            in.readerIndex(in.readerIndex() + 1);
            return;
          }
        }
      }
      if (in.readableBytes() < 5) {
        break;
      }
      int beginIdx = in.readerIndex();
      byte id = in.getByte(beginIdx);
      int length = in.getInt(beginIdx + 1);
      int endIdx = beginIdx + length + 1;
      if (in.writerIndex() < endIdx) {
        break;
      }
      ByteBuf buff = in.slice(beginIdx + 5, length - 4);
      try {
        decodeMessage(id, buff, out);
      } finally {
        in.readerIndex(endIdx);
      }
    }
  }

  private void decodeMessage(byte id, ByteBuf in, List<Object> out) {
    switch (id) {
      case ERROR_RESPONSE: {
        decodeErrorOrNotice(ErrorResponse.INSTANCE, in, out);
        break;
      }
      case NOTICE_RESPONSE: {
        decodeErrorOrNotice(NoticeResponse.INSTANCE, in, out);
        break;
      }
      case AUTHENTICATION: {
        decodeAuthentication(in, out);
      }
      break;
      case READY_FOR_QUERY: {
        decodeReadyForQuery(in, out);
        DecodeContext decodeCtx = decodeQueue.poll();
        if (decodeCtx == null) {
          throw new AssertionError(); // For debugging purposes
        }
      }
      break;
      case ROW_DESCRIPTION: {
        Column[] columns = decodeRowDescription(in);
        rowDesc = new RowDescription(columns);
        out.add(rowDesc);
      }
      break;
      case DATA_ROW: {
        DecodeContext decodeCtx = decodeQueue.peek();
        RowDescription desc = decodeCtx.peekDesc ? rowDesc : decodeCtx.rowDesc;
        JsonArray row = decodeDataRow(in, desc, decodeCtx.dataFormat);
        rows.add(row);
      }
      break;
      case COMMAND_COMPLETE: {
        decodeCommandComplete(in, rows, out);
        rows = new ArrayList<>();
      }
      break;
      case EMPTY_QUERY_RESPONSE: {
        decodeEmptyQueryResponse(out);
      }
      break;
      case PARSE_COMPLETE: {
        decodeParseComplete(out);
      }
      break;
      case BIND_COMPLETE: {
        decodeBindComplete(out);
      }
      break;
      case CLOSE_COMPLETE: {
        decodeCloseComplete(out);
      }
      break;
      case NO_DATA: {
        decodeNoData(out);
      }
      break;
      case PORTAL_SUSPENDED: {
        decodePortalSuspended(out, rows);
        rows = new ArrayList<>();
      }
      break;
      case PARAMETER_DESCRIPTION: {
        decodeParameterDescription(in, out);
      }
      break;
      case PARAMETER_STATUS: {
        decodeParameterStatus(in, out);
      }
      break;
      case BACKEND_KEY_DATA: {
        decodeBackendKeyData(in, out);
      }
      break;
      case NOTIFICATION_RESPONSE: {
        decodeNotificationResponse(in, out);
      }
      break;
    }
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

  private void decodeCommandComplete(ByteBuf in, List<JsonArray> rows, List<Object> out) {

    final byte SPACE = 32;

    int rowsAffected = 0;

    int spaceIdx1 = in.indexOf(in.readerIndex(), in.writerIndex(), SPACE);
    int prefixLen = spaceIdx1 - in.readerIndex();

    if (spaceIdx1 == -1) {
      out.add(new CommandComplete(in.toString(UTF_8), rowsAffected, rows));
      return;
    }

    int spaceIdx2 = in.indexOf(spaceIdx1 + 1, in.writerIndex(), SPACE);
    if (spaceIdx2 == -1) {
      String command = in.toString(in.readerIndex(), prefixLen, UTF_8);
      switch (command) {
        case SELECT: {
          out.add(new CommandComplete(command, rowsAffected, rows));
        }
        break;
        case UPDATE:
        case DELETE:
        case MOVE:
        case FETCH:
        case COPY: {
          rowsAffected = Integer.parseInt
            (in.toString(spaceIdx1 + 1, in.writerIndex() - spaceIdx1 - 2, UTF_8));
          out.add(new CommandComplete(command, rowsAffected, rows));
        }
        break;
        default:
          break;
      }
    }

    String command = in.toString(in.readerIndex(), prefixLen, UTF_8);
    switch (command) {
      case INSERT: {
        // Todo try to remove this slice operation
        ByteBuf otherByteBuf = in.slice(spaceIdx1 + 1, in.writerIndex() - spaceIdx1 - 2);
        int otherSpace = otherByteBuf.indexOf(otherByteBuf.readerIndex(), otherByteBuf.writerIndex(), SPACE);
        // we may need to send the oid in the message
//        ByteBuf oidBuf = otherByteBuf.slice(0, otherSpace);
        String affectedRowsByteBuf = otherByteBuf.toString(otherSpace + 1,
          otherByteBuf.writerIndex() - otherSpace - 1, UTF_8);
        rowsAffected = Integer.parseInt(affectedRowsByteBuf);
        out.add(new CommandComplete(command, rowsAffected, rows));
      }
      break;
      default:
        // ignore other SQL commands
        break;
    }
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

  private JsonArray decodeDataRow(ByteBuf in, RowDescription rowDesc, DataFormat format) {
    int len = in.readUnsignedShort();
    JsonArray row = new JsonArray(new ArrayList(len));
    for (int c = 0; c < len; ++c) {
      int length = in.readInt();
      if (length != -1) {
        Column columnDesc = rowDesc.getColumns()[c];
        DataType dataType = columnDesc.getDataType();
        Object decoded;
        if (format == DataFormat.TEXT) {
          decoded = dataType.decodeText(length, in);
        } else {
          decoded = dataType.decodeBinary(length, in);
        }
        if(decoded != null) {
          row.add(decoded);
        } else {
         row.addNull();
        }
      } else {
        row.addNull();
      }
    }
    return row;
  }

  private void decodeReadyForQuery(ByteBuf in, List<Object> out) {
    out.add(new ReadyForQuery(TransactionStatus.valueOf(in.readByte())));
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

  private void decodePortalSuspended(List<Object> out, List<JsonArray> rows) {
    out.add(new PortalSuspended(rows));
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

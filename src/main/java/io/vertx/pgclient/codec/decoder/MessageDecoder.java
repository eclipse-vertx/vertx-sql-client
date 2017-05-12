package io.vertx.pgclient.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.vertx.pgclient.codec.decoder.message.AuthenticationClearTextPassword;
import io.vertx.pgclient.codec.decoder.message.AuthenticationMD5Password;
import io.vertx.pgclient.codec.decoder.message.AuthenticationOk;
import io.vertx.pgclient.codec.decoder.message.Column;
import io.vertx.pgclient.codec.decoder.message.ColumnFormat;
import io.vertx.pgclient.codec.decoder.message.ColumnType;
import io.vertx.pgclient.codec.decoder.message.CommandComplete;
import io.vertx.pgclient.codec.decoder.message.DataRow;
import io.vertx.pgclient.codec.decoder.message.ErrorResponse;
import io.vertx.pgclient.codec.decoder.message.NoticeResponse;
import io.vertx.pgclient.codec.decoder.message.ReadyForQuery;
import io.vertx.pgclient.codec.decoder.message.Response;
import io.vertx.pgclient.codec.decoder.message.RowDescription;
import io.vertx.pgclient.codec.decoder.message.TransactionStatus;

import java.util.List;

import static io.vertx.pgclient.codec.decoder.message.type.AuthenticationType.*;
import static io.vertx.pgclient.codec.decoder.message.type.CommandCompleteType.*;
import static io.vertx.pgclient.codec.decoder.message.type.ErrorOrNoticeType.*;
import static io.vertx.pgclient.codec.decoder.message.type.MessageType.*;
import static io.vertx.pgclient.codec.decoder.message.type.ReadyForQueryTransactionStatusType.*;
import static io.vertx.pgclient.codec.util.Util.*;
import static java.nio.charset.StandardCharsets.*;

/**
 *
 * Decoder for <a href="https://www.postgresql.org/docs/9.5/static/protocol.html">PostgreSQL protocol</a>
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class MessageDecoder extends ByteToMessageDecoder {

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

    if (in.readableBytes() >= 5) {
      byte id = in.getByte(0);
      int length = in.getInt(1);
      int beginIdx = in.readerIndex();
      int endIdx = beginIdx + length + 1;
      if (in.writerIndex() >= endIdx) {
        try {
          in.readerIndex(beginIdx + 5);
          switch (id) {
            case ERROR_RESPONSE: {
              decodeErrorOrNotice(new ErrorResponse(), in, out);
              break;
            }
            case NOTICE_RESPONSE: {
              decodeErrorOrNotice(new NoticeResponse(), in, out);
              break;
            }
            case AUTHENTICATION: {
              decodeAuthentication(in, out);
            }
            break;
            case READY_FOR_QUERY: {
              decodeReadyForQuery(in, out);
            }
            break;
            case ROW_DESCRIPTION: {
              decodeRowDescription(in, out);
            }
            break;
            case DATA_ROW: {
              decodeDataRow(in, out);
            }
            break;
            case COMMAND_COMPLETE: {
              decodeCommandComplete(in, out);
            }
            break;
          }
        } finally {
          in.readerIndex(endIdx);
        }
      }
    }
  }

  private void decodeErrorOrNotice(Response response, ByteBuf data, List<Object> out) {

    byte type;

    while ((type = data.readByte()) != 0) {

      switch (type) {

        case SEVERITY:
          response.setSeverity(readCString(data, UTF_8));
          break;

        case CODE:
          response.setCode(readCString(data, UTF_8));
          break;

        case MESSAGE:
          response.setMessage(readCString(data, UTF_8));
          break;

        case DETAIL:
          response.setDetail(readCString(data, UTF_8));
          break;

        case HINT:
          response.setHint(readCString(data, UTF_8));
          break;

        case INTERNAL_POSITION:
          response.setInternalPosition(readCString(data, UTF_8));
          break;

        case INTERNAL_QUERY:
          response.setInternalQuery(readCString(data, UTF_8));
          break;

        case POSITION:
          response.setPosition(readCString(data, UTF_8));
          break;

        case WHERE:
          response.setWhere(readCString(data, UTF_8));
          break;

        case FILE:
          response.setFile(readCString(data, UTF_8));
          break;

        case LINE:
          response.setLine(readCString(data, UTF_8));
          break;

        case ROUTINE:
          response.setRoutine(readCString(data, UTF_8));
          break;

        case SCHEMA:
          response.setSchema(readCString(data, UTF_8));
          break;

        case TABLE:
          response.setTable(readCString(data, UTF_8));
          break;

        case COLUMN:
          response.setColumn(readCString(data, UTF_8));
          break;

        case DATA_TYPE:
          response.setDataType(readCString(data, UTF_8));
          break;

        case CONSTRAINT:
          response.setConstraint(readCString(data, UTF_8));
          break;

        default:
          readCString(data, UTF_8);
          break;
      }
    }
    out.add(response);
  }

  private void decodeAuthentication(ByteBuf data, List<Object> out) {

    int type = data.readInt();
    switch (type) {
      case OK: {
        out.add(new AuthenticationOk());
      }
      break;
      case MD5_PASSWORD: {
        byte[] salt = new byte[4];
        data.readBytes(salt);
        out.add(new AuthenticationMD5Password(salt));
      }
      break;
      case CLEARTEXT_PASSWORD: {
        out.add(new AuthenticationClearTextPassword());
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

  private void decodeCommandComplete(ByteBuf data, List<Object> out) {

    final byte SPACE = 32;

    int rowsAffected = 0;

    int spaceIdx1 = data.indexOf(data.readerIndex(), data.writerIndex(), SPACE);
    int prefixLen = spaceIdx1 - data.readerIndex();

    if (spaceIdx1 == -1) {
      out.add(new CommandComplete(data.toString(UTF_8), rowsAffected));
      return;
    }

    int spaceIdx2 = data.indexOf(spaceIdx1 + 1, data.writerIndex(), SPACE);
    if (spaceIdx2 == -1) {
      String command = data.retainedSlice(data.readerIndex(), prefixLen).toString(UTF_8);
      switch (command) {
        case SELECT: {
          out.add(new CommandComplete(command, rowsAffected));
        }
        break;
        case UPDATE:
        case DELETE:
        case MOVE:
        case FETCH:
        case COPY: {
          rowsAffected = Integer.parseInt
            (data.retainedSlice(spaceIdx1 + 1, data.writerIndex() - spaceIdx1 - 2).toString(UTF_8));
          out.add(new CommandComplete(command, rowsAffected));
        }
        break;
        default:
          break;
      }
    }

    String command = data.retainedSlice(data.readerIndex(), prefixLen).toString(UTF_8);
    switch (command) {
      case INSERT: {
        ByteBuf otherByteBuf = data.retainedSlice(spaceIdx1 + 1, data.writerIndex() - spaceIdx1 - 2);
        int otherSpace = otherByteBuf.indexOf(otherByteBuf.readerIndex(), otherByteBuf.writerIndex(), SPACE);
        // we may need to send the oid in the message
        ByteBuf oidBuf = otherByteBuf.retainedSlice(0, otherSpace);
        ByteBuf affectedRowsByteBuf = otherByteBuf.retainedSlice(otherSpace + 1,
          otherByteBuf.writerIndex() - otherSpace - 1);
        rowsAffected = Integer.parseInt(affectedRowsByteBuf.toString(UTF_8));
        out.add(new CommandComplete(command, rowsAffected));
      }
      break;
      default:
        // ignore other SQL commands
        break;
    }
  }

  private void decodeRowDescription(ByteBuf data, List<Object> out) {
    int columnNo = data.readUnsignedShort();
    Column[] columns = new Column[columnNo];
    for (int c = 0; c < columnNo; ++c) {
      String name = readCString(data, UTF_8);
      int relationId = data.readInt();
      short relationAttributeNo = (short) data.readUnsignedShort();
      int type = data.readInt();
      short typeLength = data.readShort();
      int typeModifier = data.readInt();
      short format = (short)data.readUnsignedShort();
      Column column = new Column(name,
        ColumnType.get(type),
        ColumnFormat.get(format),
        typeLength,
        relationId,
        relationAttributeNo,
        typeModifier);
      columns[c] = column;
    }
    out.add(new RowDescription(columns));
  }

  private void decodeDataRow(ByteBuf data, List<Object> out) {
    byte[][] values = new byte[data.readShort()][];
    for (int i = 0; i < values.length; i++) {
      int length = data.readInt();
      if (length != -1) {
        values[i] = new byte[length];
        data.readBytes(values[i]);
      } else {
        values[i] = null;
      }
    }
    out.add(new DataRow(values));
  }

  private void decodeReadyForQuery(ByteBuf data, List<Object> out) {

    byte type = data.readByte();

    switch (type) {
      case NOT_BLOCK: {
        out.add(new ReadyForQuery(TransactionStatus.NOT_BLOCK));
      }
      break;
      case BLOCK: {
        out.add(new ReadyForQuery(TransactionStatus.BLOCK));
      }
      break;
      case FAILED: {
        out.add(new ReadyForQuery(TransactionStatus.FAILED));
      }
      break;
      default:
        break;
    }
  }
}

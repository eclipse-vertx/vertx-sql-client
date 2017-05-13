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

  private void decodeErrorOrNotice(Response response, ByteBuf in, List<Object> out) {

    byte type;

    while ((type = in.readByte()) != 0) {

      switch (type) {

        case SEVERITY:
          response.setSeverity(readCString(in, UTF_8));
          break;

        case CODE:
          response.setCode(readCString(in, UTF_8));
          break;

        case MESSAGE:
          response.setMessage(readCString(in, UTF_8));
          break;

        case DETAIL:
          response.setDetail(readCString(in, UTF_8));
          break;

        case HINT:
          response.setHint(readCString(in, UTF_8));
          break;

        case INTERNAL_POSITION:
          response.setInternalPosition(readCString(in, UTF_8));
          break;

        case INTERNAL_QUERY:
          response.setInternalQuery(readCString(in, UTF_8));
          break;

        case POSITION:
          response.setPosition(readCString(in, UTF_8));
          break;

        case WHERE:
          response.setWhere(readCString(in, UTF_8));
          break;

        case FILE:
          response.setFile(readCString(in, UTF_8));
          break;

        case LINE:
          response.setLine(readCString(in, UTF_8));
          break;

        case ROUTINE:
          response.setRoutine(readCString(in, UTF_8));
          break;

        case SCHEMA:
          response.setSchema(readCString(in, UTF_8));
          break;

        case TABLE:
          response.setTable(readCString(in, UTF_8));
          break;

        case COLUMN:
          response.setColumn(readCString(in, UTF_8));
          break;

        case DATA_TYPE:
          response.setDataType(readCString(in, UTF_8));
          break;

        case CONSTRAINT:
          response.setConstraint(readCString(in, UTF_8));
          break;

        default:
          readCString(in, UTF_8);
          break;
      }
    }
    out.add(response);
  }

  private void decodeAuthentication(ByteBuf in, List<Object> out) {

    int type = in.readInt();
    switch (type) {
      case OK: {
        out.add(new AuthenticationOk());
      }
      break;
      case MD5_PASSWORD: {
        byte[] salt = new byte[4];
        in.readBytes(salt);
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

  private void decodeCommandComplete(ByteBuf in, List<Object> out) {

    final byte SPACE = 32;

    int rowsAffected = 0;

    int spaceIdx1 = in.indexOf(in.readerIndex(), in.writerIndex(), SPACE);
    int prefixLen = spaceIdx1 - in.readerIndex();

    if (spaceIdx1 == -1) {
      out.add(new CommandComplete(in.toString(UTF_8), rowsAffected));
      return;
    }

    int spaceIdx2 = in.indexOf(spaceIdx1 + 1, in.writerIndex(), SPACE);
    if (spaceIdx2 == -1) {
      String command = in.retainedSlice(in.readerIndex(), prefixLen).toString(UTF_8);
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
            (in.retainedSlice(spaceIdx1 + 1, in.writerIndex() - spaceIdx1 - 2).toString(UTF_8));
          out.add(new CommandComplete(command, rowsAffected));
        }
        break;
        default:
          break;
      }
    }

    String command = in.retainedSlice(in.readerIndex(), prefixLen).toString(UTF_8);
    switch (command) {
      case INSERT: {
        ByteBuf otherByteBuf = in.retainedSlice(spaceIdx1 + 1, in.writerIndex() - spaceIdx1 - 2);
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

  private void decodeRowDescription(ByteBuf in, List<Object> out) {
    short columnNo = in.readShort();
    Column[] columns = new Column[columnNo];
    for (short c = 0; c < columnNo; ++c) {
      String name = readCString(in, UTF_8);
      int relationId = in.readInt();
      short relationAttributeNo = in.readShort();
      int type = in.readInt();
      short typeLength = in.readShort();
      int typeModifier = in.readInt();
      short format = in.readShort();
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

  private void decodeDataRow(ByteBuf in, List<Object> out) {
    byte[][] values = new byte[in.readShort()][];
    for (short c = 0; c < values.length; ++c) {
      int length = in.readInt();
      if (length != -1) {
        values[c] = new byte[length];
        in.readBytes(values[c]);
      } else {
        values[c] = null;
      }
    }
    out.add(new DataRow(values));
  }

  private void decodeReadyForQuery(ByteBuf in, List<Object> out) {

    byte type = in.readByte();

    switch (type) {
      case IDLE: {
        out.add(new ReadyForQuery(TransactionStatus.IDLE));
      }
      break;
      case ACTIVE: {
        out.add(new ReadyForQuery(TransactionStatus.ACTIVE));
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

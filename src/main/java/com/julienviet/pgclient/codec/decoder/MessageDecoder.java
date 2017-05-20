package com.julienviet.pgclient.codec.decoder;

import com.julienviet.pgclient.codec.Column;
import com.julienviet.pgclient.codec.DataFormat;
import com.julienviet.pgclient.codec.DataType;
import com.julienviet.pgclient.codec.TransactionStatus;
import com.julienviet.pgclient.codec.decoder.message.AuthenticationClearTextPassword;
import com.julienviet.pgclient.codec.decoder.message.AuthenticationMD5Password;
import com.julienviet.pgclient.codec.decoder.message.AuthenticationOk;
import com.julienviet.pgclient.codec.decoder.message.BackendKeyData;
import com.julienviet.pgclient.codec.decoder.message.BindComplete;
import com.julienviet.pgclient.codec.decoder.message.CloseComplete;
import com.julienviet.pgclient.codec.decoder.message.CommandComplete;
import com.julienviet.pgclient.codec.decoder.message.DataRow;
import com.julienviet.pgclient.codec.decoder.message.EmptyQueryResponse;
import com.julienviet.pgclient.codec.decoder.message.ErrorResponse;
import com.julienviet.pgclient.codec.decoder.message.NoData;
import com.julienviet.pgclient.codec.decoder.message.NoticeResponse;
import com.julienviet.pgclient.codec.decoder.message.NotificationResponse;
import com.julienviet.pgclient.codec.decoder.message.ParameterDescription;
import com.julienviet.pgclient.codec.decoder.message.ParameterStatus;
import com.julienviet.pgclient.codec.decoder.message.ParseComplete;
import com.julienviet.pgclient.codec.decoder.message.PortalSuspended;
import com.julienviet.pgclient.codec.decoder.message.ReadyForQuery;
import com.julienviet.pgclient.codec.decoder.message.Response;
import com.julienviet.pgclient.codec.decoder.message.RowDescription;
import com.julienviet.pgclient.codec.util.Util;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

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
              decodePortalSuspended(out);
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
    Column[] columns = new Column[in.readUnsignedShort()];
    for (int c = 0; c < columns.length; ++c) {
      Column column = new Column(
        Util.readCStringUTF8(in),
        in.readInt(),
        in.readShort(),
        DataType.valueOf(in.readInt()),
        in.readShort(),
        in.readInt(),
        DataFormat.valueOf(in.readUnsignedShort())
      );
      columns[c] = column;
    }
    out.add(new RowDescription(columns));
  }

  private void decodeDataRow(ByteBuf in, List<Object> out) {
    byte[][] values = new byte[in.readUnsignedShort()][];
    for (int c = 0; c < values.length; ++c) {
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

  private void decodePortalSuspended(List<Object> out) {
    out.add(PortalSuspended.INSTANCE);
  }

  private void decodeParameterDescription(ByteBuf in, List<Object> out) {
    int[] paramDataTypes = new int[in.readUnsignedShort()];
    for (int c = 0; c < paramDataTypes.length; ++c) {
      paramDataTypes[c] = in.readInt();
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

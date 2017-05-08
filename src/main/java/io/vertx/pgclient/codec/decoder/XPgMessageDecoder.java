package io.vertx.pgclient.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.vertx.pgclient.codec.decoder.message.AuthenticationClearTextPasswordMessage;
import io.vertx.pgclient.codec.decoder.message.AuthenticationMD5PasswordMessage;
import io.vertx.pgclient.codec.decoder.message.AuthenticationOkMessage;
import io.vertx.pgclient.codec.decoder.message.CommandCompleteMessage;
import io.vertx.pgclient.codec.decoder.message.DataRowMessage;
import io.vertx.pgclient.codec.decoder.message.ErrorResponseMessage;
import io.vertx.pgclient.codec.decoder.message.NoticeResponseMessage;
import io.vertx.pgclient.codec.decoder.message.ReadyForQueryMessage;
import io.vertx.pgclient.codec.decoder.message.RowDescriptionMessage;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static io.vertx.pgclient.codec.utils.Utils.*;
import static java.nio.charset.StandardCharsets.*;

public class XPgMessageDecoder extends ByteToMessageDecoder {

  // Backend types
  private static final byte BACKEND_KEY_DATA = 'K';
  private static final byte AUTHENTICATION = 'R';
  private static final byte ERROR_RESPONSE = 'E';
  private static final byte NOTICE_RESPONSE = 'N';
  private static final byte NOTIFICATION_RESPONSE = 'A';
  private static final byte COMMAND_COMPLETE = 'C';
  private static final byte PARAMETER_STATUS = 'S';
  private static final byte READY_FOR_QUERY = 'Z';
  private static final byte PARAMETER_DESC_MSG_ID = 't';
  private static final byte ROW_DESCRIPTION = 'T';
  private static final byte DATA_ROW = 'D';
  private static final byte PORTAL_SUSPENDED_MSG_ID = 's';
  private static final byte NO_DATA_MSG_ID = 'n';
  private static final byte EMPTY_QUERY_MSG_ID = 'I';
  private static final byte PARSE_COMPLETE = '1';
  private static final byte BIND_COMPLETE = '2';
  private static final byte CLOSE_COMPLETE = '3';
  private static final byte FUNCTION_RESULT = 'V';

  // Authentication sub types
  private static final int AUTHENTICATION_OK = 0;
  private static final int AUTHENTICATION_KERBEROS_V5 = 2;
  private static final int AUTHENTICATION_CLEARTEXT_PASSWORD = 3;
  private static final int AUTHENTICATION_MD5_PASSWORD = 5;
  private static final int AUTHENTICATION_SCM_CREDENTIAL = 6;
  private static final int AUTHENTICATION_GSS = 7;
  private static final int AUTHENTICATION_GSS_CONTINUE = 8;
  private static final int AUTHENTICATION_SSPI = 9;

  // Error and notice sub Types
  private static final byte SEVERITY = 'S';
  private static final byte CODE = 'C';
  private static final byte MESSAGE = 'M';
  private static final byte DETAIL = 'D';
  private static final byte HINT = 'H';
  private static final byte POSITION = 'P';
  private static final byte WHERE = 'W';
  private static final byte FILE = 'F';
  private static final byte LINE = 'L';
  private static final byte ROUTINE = 'R';
  private static final byte SCHEMA = 's';
  private static final byte TABLE = 't';
  private static final byte COLUMN = 'c';
  private static final byte DATA_TYPE = 'd';
  private static final byte CONSTRAINT = 'n';

  @Override
  protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {

    if (in.readableBytes() == 0) {
      return;
    }

    int BACKEND_MESSAGE_TYPE = in.readByte();
    int len = in.readInt();


    switch (BACKEND_MESSAGE_TYPE) {
      case ERROR_RESPONSE: {
        ErrorResponseMessage error = new ErrorResponseMessage();
        byte errorType;
        while ((errorType = in.readByte()) != 0) {
          switch (errorType) {
            case SEVERITY:
              error.setSeverity(readCString(in, UTF_8));
              break;

            case CODE:
              error.setCode(readCString(in, UTF_8));
              break;

            case MESSAGE:
              error.setMessage(readCString(in, UTF_8));
              break;

            case DETAIL:
              error.setDetail(readCString(in, UTF_8));
              break;

            case HINT:
              error.setHint(readCString(in, UTF_8));
              break;

            case POSITION:
              error.setPosition(readCString(in, UTF_8));
              break;

            case WHERE:
              error.setWhere(readCString(in, UTF_8));
              break;

            case FILE:
              error.setFile(readCString(in, UTF_8));
              break;

            case LINE:
              error.setLine(readCString(in, UTF_8));
              break;

            case ROUTINE:
              error.setRoutine(readCString(in, UTF_8));
              break;

            case SCHEMA:
              error.setSchema(readCString(in, UTF_8));
              break;

            case TABLE:
              error.setTable(readCString(in, UTF_8));
              break;

            case COLUMN:
              error.setColumn(readCString(in, UTF_8));
              break;

            case DATA_TYPE:
              error.setDataType(readCString(in, UTF_8));
              break;

            case CONSTRAINT:
              error.setConstraint(readCString(in, UTF_8));
              break;

            default:
              readCString(in, UTF_8);
              break;
          }
        }
        out.add(error);
        break;
      }
      case NOTICE_RESPONSE: {
        NoticeResponseMessage notice = new NoticeResponseMessage();
        byte noticeType;
        while ((noticeType = in.readByte()) != 0) {
          switch (noticeType) {
            case SEVERITY:
              notice.setSeverity(readCString(in, UTF_8));
              break;

            case CODE:
              notice.setCode(readCString(in, UTF_8));
              break;

            case MESSAGE:
              notice.setMessage(readCString(in, UTF_8));
              break;

            case DETAIL:
              notice.setDetail(readCString(in, UTF_8));
              break;

            case HINT:
              notice.setHint(readCString(in, UTF_8));
              break;

            case POSITION:
              notice.setPosition(readCString(in, UTF_8));
              break;

            case WHERE:
              notice.setWhere(readCString(in, UTF_8));
              break;

            case FILE:
              notice.setFile(readCString(in, UTF_8));
              break;

            case LINE:
              notice.setLine(readCString(in, UTF_8));
              break;

            case ROUTINE:
              notice.setRoutine(readCString(in, UTF_8));
              break;

            case SCHEMA:
              notice.setSchema(readCString(in, UTF_8));
              break;

            case TABLE:
              notice.setTable(readCString(in, UTF_8));
              break;

            case COLUMN:
              notice.setColumn(readCString(in, UTF_8));
              break;

            case DATA_TYPE:
              notice.setDataType(readCString(in, UTF_8));
              break;

            case CONSTRAINT:
              notice.setConstraint(readCString(in, UTF_8));
              break;

            default:
              readCString(in, UTF_8);
              break;
          }
        }
        out.add(notice);
        break;
      }
      case AUTHENTICATION: {
        int AUTHENTICATION_TYPE = in.readInt();
        switch (AUTHENTICATION_TYPE) {
          case AUTHENTICATION_OK: {
            out.add(new AuthenticationOkMessage());
          }
          break;
          case AUTHENTICATION_MD5_PASSWORD: {
            byte[] salt = new byte[4];
            in.readBytes(salt);
            out.add(new AuthenticationMD5PasswordMessage(salt));
          }
          break;
          case AUTHENTICATION_CLEARTEXT_PASSWORD: {
            out.add(new AuthenticationClearTextPasswordMessage());
          }
          break;
          case AUTHENTICATION_KERBEROS_V5:
          case AUTHENTICATION_SCM_CREDENTIAL:
          case AUTHENTICATION_GSS:
          case AUTHENTICATION_GSS_CONTINUE:
          case AUTHENTICATION_SSPI:
          default:
            throw new UnsupportedOperationException("Authentication type is not supported in the client");
        }
      }
      break;
      case READY_FOR_QUERY: {
        out.add(new ReadyForQueryMessage());
      }
      break;
      case ROW_DESCRIPTION: {
        System.out.println("ROW_DESCRIPTION");
        int columnNo = in.readUnsignedShort();
        Column[] columns = new Column[columnNo];
        for (int c = 0; c < columnNo; ++c) {
          String name = readCString(in, UTF_8);
          int relationId = in.readInt();
          short relationAttributeNo = (short) in.readUnsignedShort();
          int type = in.readInt();
          short typeLength = in.readShort();
          int typeModifier = in.readInt();
          short format = (short)in.readUnsignedShort();
          Column column = new Column(name,
            ColumnType.get(type),
            ColumnFormat.get(format),
            typeLength,
            relationId,
            relationAttributeNo,
            typeModifier);
          columns[c] = column;
        }
        out.add(new RowDescriptionMessage(columns));
      }
      break;
      case DATA_ROW: {
        byte[][] values = new byte[in.readShort()][];
        for (int i = 0; i < values.length; i++) {
          int length = in.readInt();
          if (length != -1) {
            values[i] = new byte[length];
            in.readBytes(values[i]);
          } else {
            values[i] = null;
          }
        }
        out.add(new DataRowMessage(values));
      }
      break;
      case COMMAND_COMPLETE: {
        String tag = readCString(in, UTF_8);
        String[] parts = tag.split(" ");
        String command = parts[0];
        int rowsAffected = 0;
        switch (command) {
          case "INSERT":
            if (parts.length == 3) {
              rowsAffected = Integer.parseInt(parts[2]);
            } else {
              throw new IOException("error parsing command tag: " + command + " (" + Arrays.toString(parts) + ")");
            }
            break;
          case "SELECT":
            if (parts.length == 2) {
              rowsAffected = 0;
            } else {
              throw new IOException("error parsing command tag: " + command + " (" + Arrays.toString(parts) + ")");
            }
            break;
          case "UPDATE":
          case "DELETE":
          case "MOVE":
          case "FETCH":
            if (parts.length == 2) {
              rowsAffected = Integer.parseInt(parts[1]);
            } else {
              throw new IOException("error parsing command tag: " + command + " (" + Arrays.toString(parts) + ")");
            }
            break;
          case "COPY":
            if (parts.length == 1) {
            } else if (parts.length == 2) {
              rowsAffected = Integer.parseInt(parts[1]);
            } else {
              throw new IOException("error parsing command tag: " + command + " (" + Arrays.toString(parts) + ")");
            }
            break;
          case "CREATE":
          case "DROP":
          case "ALTER":
          case "DECLARE":
          case "CLOSE":
            if (parts.length == 2) {
              command += " " + parts[1];
              rowsAffected = 0;
            } else if (parts.length == 3) {
              command += " " + parts[1] + " " + parts[2];
              rowsAffected = 0;
            } else if (parts.length == 4) {
              command += " " + parts[1] + " " + parts[2] + " " + parts[3];
              rowsAffected = 0;
            } else {
              throw new IOException("error parsing command tag: " + command + " (" + Arrays.toString(parts) + ")");
            }
            break;
          case "PREPARE":
            if (parts.length == 2) {
            } else {
              throw new IOException("error parsing command tag: " + command + " (" + Arrays.toString(parts) + ")");
            }
            break;

          case "COMMIT":
            if (parts.length == 1 || parts.length == 2) {
            } else {
              throw new IOException("error parsing command tag: " + command + " (" + Arrays.toString(parts) + ")");
            }
            break;

          case "ROLLBACK":
            if (parts.length == 1 || parts.length == 2) {
            } else {
              throw new IOException("error parsing command tag: " + command + " (" + Arrays.toString(parts) + ")");
            }
            break;

          case "DEALLOCATE":
          case "TRUNCATE":
          case "LOCK":
          case "GRANT":
          case "REVOKE":
            break;
          default:
            rowsAffected = 0;
        }
        out.add(new CommandCompleteMessage(command, rowsAffected));
      }
      break;
    }
  }
}

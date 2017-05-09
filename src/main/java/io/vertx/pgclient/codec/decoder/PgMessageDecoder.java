package io.vertx.pgclient.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import io.vertx.pgclient.codec.decoder.message.AuthenticationClearTextPasswordMessage;
import io.vertx.pgclient.codec.decoder.message.AuthenticationMD5PasswordMessage;
import io.vertx.pgclient.codec.decoder.message.AuthenticationOkMessage;
import io.vertx.pgclient.codec.decoder.message.CommandCompleteMessage;
import io.vertx.pgclient.codec.decoder.message.DataRowMessage;
import io.vertx.pgclient.codec.decoder.message.ErrorResponseMessage;
import io.vertx.pgclient.codec.decoder.message.NoticeResponseMessage;
import io.vertx.pgclient.codec.decoder.message.ReadyForQueryMessage;
import io.vertx.pgclient.codec.decoder.message.ResponseMessage;
import io.vertx.pgclient.codec.decoder.message.RowDescriptionMessage;

import java.util.Arrays;
import java.util.List;

import static io.vertx.pgclient.codec.utils.Utils.*;
import static java.nio.charset.StandardCharsets.*;

/**
 *
 * Decoder for <a href="https://www.postgresql.org/docs/9.5/static/protocol.html">PostgreSQL protocol</a>
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class PgMessageDecoder extends ByteToMessageDecoder {

  // Backend message id
  private static final byte BACKEND_KEY_DATA = 'K';
  private static final byte AUTHENTICATION = 'R';
  private static final byte ERROR_RESPONSE = 'E';
  private static final byte NOTICE_RESPONSE = 'N';
  private static final byte NOTIFICATION_RESPONSE = 'A';
  private static final byte COMMAND_COMPLETE = 'C';
  private static final byte PARAMETER_STATUS = 'S';
  private static final byte READY_FOR_QUERY = 'Z';
  private static final byte PARAMETER_DESCRIPTION = 't';
  private static final byte ROW_DESCRIPTION = 'T';
  private static final byte DATA_ROW = 'D';
  private static final byte PORTAL_SUSPENDED = 's';
  private static final byte NO_DATA = 'n';
  private static final byte EMPTY_QUERY_RESPONSE = 'I';
  private static final byte PARSE_COMPLETE = '1';
  private static final byte BIND_COMPLETE = '2';
  private static final byte CLOSE_COMPLETE = '3';
  private static final byte FUNCTION_RESULT = 'V';

  private enum State {
    HEADER,
    BODY
  }

  private State state = State.HEADER;
  private byte id;
  private int length;

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    switch (state) {
      case HEADER:
        if (in.readableBytes() < 5) {
          return;
        }
        id = in.readByte();
        length = in.readInt() - 4;
        state = State.BODY;
      case BODY:
        if (in.readableBytes() < length) {
          return;
        }
        ByteBuf data = in.readBytes(length);
        state = State.HEADER;
        switch (id) {
          case ERROR_RESPONSE: {
            decodeErrorOrNotice(new ErrorResponseMessage(), data, out);
            break;
          }
          case NOTICE_RESPONSE: {
            decodeErrorOrNotice(new NoticeResponseMessage(), data, out);
            break;
          }
          case AUTHENTICATION: {
            decodeAuthentication(data, out);
          }
          break;
          case READY_FOR_QUERY: {
            decodeReadyForQuery(data, out);
          }
          break;
          case ROW_DESCRIPTION: {
            decodeRowDescription(data, out);
          }
          break;
          case DATA_ROW: {
            decodeDataRow(data, out);
          }
          break;
          case COMMAND_COMPLETE: {
            decodeCommandComplete(data, out);
          }
          break;
        }
    }
  }

  private void decodeErrorOrNotice(ResponseMessage responseMessage, ByteBuf data, List<Object> out) {

    final byte SEVERITY = 'S';
    final byte CODE = 'C';
    final byte MESSAGE = 'M';
    final byte DETAIL = 'D';
    final byte HINT = 'H';
    final byte POSITION = 'P';
    final byte INTERNAL_POSITION = 'p';
    final byte INTERNAL_QUERY = 'q';
    final byte WHERE = 'W';
    final byte FILE = 'F';
    final byte LINE = 'L';
    final byte ROUTINE = 'R';
    final byte SCHEMA = 's';
    final byte TABLE = 't';
    final byte COLUMN = 'c';
    final byte DATA_TYPE = 'd';
    final byte CONSTRAINT = 'n';

    byte type;

    while ((type = data.readByte()) != 0) {

      switch (type) {

        case SEVERITY:
          responseMessage.setSeverity(readCString(data, UTF_8));
          break;

        case CODE:
          responseMessage.setCode(readCString(data, UTF_8));
          break;

        case MESSAGE:
          responseMessage.setMessage(readCString(data, UTF_8));
          break;

        case DETAIL:
          responseMessage.setDetail(readCString(data, UTF_8));
          break;

        case HINT:
          responseMessage.setHint(readCString(data, UTF_8));
          break;

        case INTERNAL_POSITION:
          responseMessage.setInternalPosition(readCString(data, UTF_8));
          break;

        case INTERNAL_QUERY:
          responseMessage.setInternalQuery(readCString(data, UTF_8));
          break;

        case POSITION:
          responseMessage.setPosition(readCString(data, UTF_8));
          break;

        case WHERE:
          responseMessage.setWhere(readCString(data, UTF_8));
          break;

        case FILE:
          responseMessage.setFile(readCString(data, UTF_8));
          break;

        case LINE:
          responseMessage.setLine(readCString(data, UTF_8));
          break;

        case ROUTINE:
          responseMessage.setRoutine(readCString(data, UTF_8));
          break;

        case SCHEMA:
          responseMessage.setSchema(readCString(data, UTF_8));
          break;

        case TABLE:
          responseMessage.setTable(readCString(data, UTF_8));
          break;

        case COLUMN:
          responseMessage.setColumn(readCString(data, UTF_8));
          break;

        case DATA_TYPE:
          responseMessage.setDataType(readCString(data, UTF_8));
          break;

        case CONSTRAINT:
          responseMessage.setConstraint(readCString(data, UTF_8));
          break;

        default:
          readCString(data, UTF_8);
          break;
      }
    }
    out.add(responseMessage);
  }

  private void decodeAuthentication(ByteBuf data, List<Object> out) {

    final int AUTHENTICATION_OK = 0;
    final int AUTHENTICATION_KERBEROS_V5 = 2;
    final int AUTHENTICATION_CLEARTEXT_PASSWORD = 3;
    final int AUTHENTICATION_MD5_PASSWORD = 5;
    final int AUTHENTICATION_SCM_CREDENTIAL = 6;
    final int AUTHENTICATION_GSS = 7;
    final int AUTHENTICATION_GSS_CONTINUE = 8;
    final int AUTHENTICATION_SSPI = 9;

    int AUTHENTICATION_TYPE = data.readInt();
    switch (AUTHENTICATION_TYPE) {
      case AUTHENTICATION_OK: {
        out.add(new AuthenticationOkMessage());
      }
      break;
      case AUTHENTICATION_MD5_PASSWORD: {
        byte[] salt = new byte[4];
        data.readBytes(salt);
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

  private void decodeCommandComplete(ByteBuf data, List<Object> out) {

    final String INSERT = "INSERT";
    final String SELECT = "SELECT";
    final String UPDATE = "UPDATE";
    final String DELETE = "DELETE";
    final String MOVE = "MOVE";
    final String FETCH = "FETCH";
    final String COPY = "COPY";
    final String CREATE = "CREATE";
    final String DROP = "DROP";
    final String ALTER = "ALTER";
    final String DECLARE = "DECLARE";
    final String CLOSE = "CLOSE";
    final String PREPARE = "PREPARE";
    final String COMMIT = "COMMIT";
    final String ROLLBACK = "ROLLBACK";
    final String DEALLOCATE = "DEALLOCATE";
    final String TRUNCATE = "TRUNCATE";
    final String LOCK = "LOCK";
    final String GRANT = "GRANT";
    final String REVOKE = "REVOKE";


    String tag = readCString(data, UTF_8);
    String[] parts = tag.split(" ");
    String command = parts[0];
    int rowsAffected = 0;
    switch (command) {
      case INSERT:
        if (parts.length == 3) {
          rowsAffected = Integer.parseInt(parts[2]);
        } else {
          throw new DecoderException("error parsing command tag: " + command + " (" + Arrays.toString(parts) + ")");
        }
        break;
      case SELECT:
        if (parts.length == 2) {
          rowsAffected = 0;
        } else {
          throw new DecoderException("error parsing command tag: " + command + " (" + Arrays.toString(parts) + ")");
        }
        break;
      case UPDATE:
      case DELETE:
      case MOVE:
      case FETCH:
        if (parts.length == 2) {
          rowsAffected = Integer.parseInt(parts[1]);
        } else {
          throw new DecoderException("error parsing command tag: " + command + " (" + Arrays.toString(parts) + ")");
        }
        break;
      case COPY:
        if (parts.length == 1) {
        } else if (parts.length == 2) {
          rowsAffected = Integer.parseInt(parts[1]);
        } else {
          throw new DecoderException("error parsing command tag: " + command + " (" + Arrays.toString(parts) + ")");
        }
        break;
      case CREATE:
      case DROP:
      case ALTER:
      case DECLARE:
      case CLOSE:
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
          throw new DecoderException("error parsing command tag: " + command + " (" + Arrays.toString(parts) + ")");
        }
        break;
      case PREPARE:
        if (parts.length == 2) {
        } else {
          throw new DecoderException("error parsing command tag: " + command + " (" + Arrays.toString(parts) + ")");
        }
        break;

      case COMMIT:
        if (parts.length == 1 || parts.length == 2) {
        } else {
          throw new DecoderException("error parsing command tag: " + command + " (" + Arrays.toString(parts) + ")");
        }
        break;

      case ROLLBACK:
        if (parts.length == 1 || parts.length == 2) {
        } else {
          throw new DecoderException("error parsing command tag: " + command + " (" + Arrays.toString(parts) + ")");
        }
        break;

      case DEALLOCATE:
      case TRUNCATE:
      case LOCK:
      case GRANT:
      case REVOKE:
        break;
      default:
        rowsAffected = 0;
    }
    out.add(new CommandCompleteMessage(command, rowsAffected));
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
    out.add(new RowDescriptionMessage(columns));
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
    out.add(new DataRowMessage(values));
  }

  private void decodeReadyForQuery(ByteBuf data, List<Object> out) {

    final byte NOT_BLOCK = 'I';
    final byte BLOCK = 'T';
    final byte FAILED = 'E';

    byte STATUS_TYPE = data.readByte();

    switch (STATUS_TYPE) {
      case NOT_BLOCK: {
        out.add(new ReadyForQueryMessage(TransactionStatus.NOT_BLOCK));
      }
      break;
      case BLOCK: {
        out.add(new ReadyForQueryMessage(TransactionStatus.BLOCK));
      }
      break;
      case FAILED: {
        out.add(new ReadyForQueryMessage(TransactionStatus.FAILED));
      }
      break;
      default:
        break;
    }
  }
}
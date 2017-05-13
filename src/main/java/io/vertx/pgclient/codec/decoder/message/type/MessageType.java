package io.vertx.pgclient.codec.decoder.message.type;

import io.vertx.pgclient.codec.decoder.MessageDecoder;

/**
 *
 * Backend message types for {@link MessageDecoder}
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class MessageType {
  public static final byte BACKEND_KEY_DATA = 'K';
  public static final byte AUTHENTICATION = 'R';
  public static final byte ERROR_RESPONSE = 'E';
  public static final byte NOTICE_RESPONSE = 'N';
  public static final byte NOTIFICATION_RESPONSE = 'A';
  public static final byte COMMAND_COMPLETE = 'C';
  public static final byte PARAMETER_STATUS = 'S';
  public static final byte READY_FOR_QUERY = 'Z';
  public static final byte PARAMETER_DESCRIPTION = 't';
  public static final byte ROW_DESCRIPTION = 'T';
  public static final byte DATA_ROW = 'D';
  public static final byte PORTAL_SUSPENDED = 's';
  public static final byte NO_DATA = 'n';
  public static final byte EMPTY_QUERY_RESPONSE = 'I';
  public static final byte PARSE_COMPLETE = '1';
  public static final byte BIND_COMPLETE = '2';
  public static final byte CLOSE_COMPLETE = '3';
  public static final byte FUNCTION_RESULT = 'V';
}

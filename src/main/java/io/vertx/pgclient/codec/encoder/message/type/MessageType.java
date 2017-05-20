package io.vertx.pgclient.codec.encoder.message.type;

import io.vertx.pgclient.codec.encoder.MessageEncoder;

/**
 *
 * Frontend message types for {@link MessageEncoder}
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class MessageType {
  public static final byte PASSWORD_MESSAGE = 'p';
  public static final byte QUERY = 'Q';
  public static final byte TERMINATE = 'X';
  public static final byte PARSE = 'P';
  public static final byte BIND = 'B';
  public static final byte DESCRIBE = 'D';
  public static final byte EXECUTE = 'E';
  public static final byte CLOSE = 'C';
  public static final byte SYNC = 'S';
}

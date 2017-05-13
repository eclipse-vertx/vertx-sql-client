package io.vertx.pgclient.codec.encoder.message.type;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class MessageType {
  public static final byte PASSWORD_MESSAGE = 'p';
  public static final byte QUERY = 'Q';
  public static final byte TERMINATE = 'X';
}

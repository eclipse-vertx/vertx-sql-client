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
}

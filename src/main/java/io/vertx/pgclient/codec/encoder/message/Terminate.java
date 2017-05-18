package io.vertx.pgclient.codec.encoder.message;

import io.vertx.pgclient.codec.Message;

/**
 *
 * <p>
 * This message immediately closes the connection. On receipt of this message,
 * the backend closes the connection and terminates.
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class Terminate implements Message {

  @Override
  public String toString() {
    return "Terminate{}";
  }
}

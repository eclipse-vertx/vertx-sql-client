package io.vertx.pgclient.codec.encoder.message;

import io.vertx.pgclient.codec.Message;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class TerminateMessage implements Message {

  @Override
  public String toString() {
    return "TerminateMessage{}";
  }
}

package io.vertx.pgclient.codec.encoder.message;

import io.vertx.pgclient.codec.Message;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class Terminate implements Message {

  @Override
  public String toString() {
    return "Terminate{}";
  }
}

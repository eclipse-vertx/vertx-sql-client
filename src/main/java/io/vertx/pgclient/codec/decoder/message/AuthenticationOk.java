package io.vertx.pgclient.codec.decoder.message;

import io.vertx.pgclient.codec.Message;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class AuthenticationOk implements Message {

  @Override
  public String toString() {
    return "AuthenticationOk{}";
  }
}

package io.vertx.pgclient.codec.decoder.message;

import io.vertx.pgclient.codec.Message;

/**
 *
 * <p>
 * Identifies the message as a response to an empty query string. (This substitutes for {@link CommandComplete})
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class EmptyQueryResponse implements Message {

  public static final EmptyQueryResponse INSTANCE = new EmptyQueryResponse();

  private EmptyQueryResponse() {}

  @Override
  public String toString() {
    return "EmptyQueryResponse{}";
  }
}

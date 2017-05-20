package com.julienviet.pgclient.codec.decoder.message;

import com.julienviet.pgclient.codec.Message;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class ParseComplete implements Message {

  public static final ParseComplete INSTANCE = new ParseComplete();

  private ParseComplete() {}

  @Override
  public String toString() {
    return "ParseComplete{}";
  }
}

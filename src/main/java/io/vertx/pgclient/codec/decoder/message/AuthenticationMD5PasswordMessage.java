package io.vertx.pgclient.codec.decoder.message;

import io.vertx.pgclient.codec.Message;


public class AuthenticationMD5PasswordMessage implements Message {
  private final byte[] salt;
  public AuthenticationMD5PasswordMessage(byte[] salt) {
    this.salt = salt;
  }

  public byte[] getSalt() {
    return salt;
  }
}

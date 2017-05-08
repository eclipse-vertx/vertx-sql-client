package io.vertx.pgclient.codec.encoder.message;

import io.vertx.pgclient.codec.Message;
import io.vertx.pgclient.codec.utils.MD5Authentication;

public class PasswordMessage implements Message {

  final String password;
  final byte[] passwordHash;

  public PasswordMessage(String username, String password, byte[] salt) {
    this.passwordHash = salt != null ? MD5Authentication.encode(username, password, salt).getBytes() : null;
    this.password = password;
  }

  public String getPassword() {
    return password;
  }

  public byte[] getPasswordHash() {
    return passwordHash;
  }
}

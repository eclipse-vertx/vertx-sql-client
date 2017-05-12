package io.vertx.pgclient.codec.encoder.message;

import io.vertx.pgclient.codec.Message;
import io.vertx.pgclient.codec.util.MD5Authentication;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class PasswordMessage implements Message {

  final byte[] password;
  final byte[] passwordHash;

  public PasswordMessage(String username, String password, byte[] salt) {
    this.passwordHash = salt != null ? MD5Authentication.encode(username, password, salt).getBytes() : null;
    this.password = password.getBytes();
  }

  public byte[] getPassword() {
    return password;
  }

  public byte[] getPasswordHash() {
    return passwordHash;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PasswordMessage that = (PasswordMessage) o;
    return Arrays.equals(password, that.password) &&
      Arrays.equals(passwordHash, that.passwordHash);
  }

  @Override
  public int hashCode() {
    return Objects.hash(password, passwordHash);
  }


  @Override
  public String toString() {
    return "PasswordMessage{" +
      "password=" + Arrays.toString(password) +
      ", passwordHash=" + Arrays.toString(passwordHash) +
      '}';
  }
}

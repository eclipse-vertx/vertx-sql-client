package io.vertx.pgclient.codec.decoder.message;

import io.vertx.pgclient.codec.Message;

import java.util.Arrays;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class AuthenticationMD5PasswordMessage implements Message {

  private final byte[] salt;

  public AuthenticationMD5PasswordMessage(byte[] salt) {
    this.salt = salt;
  }

  public byte[] getSalt() {
    return salt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AuthenticationMD5PasswordMessage that = (AuthenticationMD5PasswordMessage) o;
    return Arrays.equals(salt, that.salt);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(salt);
  }

  @Override
  public String toString() {
    return "AuthenticationMD5PasswordMessage{" +
      "salt=" + Arrays.toString(salt) +
      '}';
  }
}

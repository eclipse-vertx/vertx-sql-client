package io.vertx.pgclient.codec.encoder.message;

import io.vertx.pgclient.codec.Message;
import io.vertx.pgclient.codec.util.MD5Authentication;

import java.util.Objects;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class PasswordMessage implements Message {

  final String text;
  final String hash;

  public PasswordMessage(String username, String password, byte[] salt) {
    this.hash = salt != null ? MD5Authentication.encode(username, password, salt) : null;
    this.text = password;
  }

  public String getText() {
    return text;
  }

  public String getHash() {
    return hash;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PasswordMessage that = (PasswordMessage) o;
    return Objects.equals(text, that.text) &&
      Objects.equals(hash, that.hash);
  }

  @Override
  public int hashCode() {
    return Objects.hash(text, hash);
  }


  @Override
  public String toString() {
    return "PasswordMessage{" +
      "text='" + text + '\'' +
      ", hash='" + hash + '\'' +
      '}';
  }
}

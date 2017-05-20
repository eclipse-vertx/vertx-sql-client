package io.vertx.pgclient.codec.decoder.message;

import io.vertx.pgclient.codec.Message;

import java.util.Objects;

/**
 * <p>
 * This message informs the frontend about the current (initial) setting of backend parameters.
 * The frontend can ignore this message, or record the settings for its future use.
 * The frontend should not respond to this message, but should continue listening for a {@link ReadyForQuery} message.
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class ParameterStatus implements Message {

  private final String key;
  private final String value;

  public ParameterStatus(String key, String value) {
    this.key = key;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ParameterStatus that = (ParameterStatus) o;
    return Objects.equals(key, that.key) &&
      Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, value);
  }

  @Override
  public String toString() {
    return "ParameterStatus{" +
      "key='" + key + '\'' +
      ", value='" + value + '\'' +
      '}';
  }

}

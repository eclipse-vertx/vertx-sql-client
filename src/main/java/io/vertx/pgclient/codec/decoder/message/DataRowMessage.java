package io.vertx.pgclient.codec.decoder.message;

import io.vertx.pgclient.codec.Message;

import java.util.Arrays;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class DataRowMessage implements Message {

  final byte[][] values;

  public DataRowMessage(byte[][] values) {
    this.values = values;
  }
  public byte[] getValue(int i) {
    return values[i];
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DataRowMessage that = (DataRowMessage) o;
    return Arrays.equals(values, that.values);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(values);
  }


  @Override
  public String toString() {
    return "DataRowMessage{" +
      "values=" + Arrays.toString(values) +
      '}';
  }
}

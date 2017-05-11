package io.vertx.pgclient.codec.decoder.message;

import io.vertx.pgclient.codec.Message;

import java.util.Arrays;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class DataRow implements Message {

  final byte[][] values;

  public DataRow(byte[][] values) {
    this.values = values;
  }
  public byte[] getValue(int i) {
    return values[i];
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DataRow that = (DataRow) o;
    return Arrays.equals(values, that.values);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(values);
  }


  @Override
  public String toString() {
    return "DataRow{" +
      "values=" + Arrays.toString(values) +
      '}';
  }
}

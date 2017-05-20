package io.vertx.pgclient.codec.decoder.message;

import io.vertx.pgclient.codec.Column;
import io.vertx.pgclient.codec.Message;

import java.util.Arrays;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class RowDescription implements Message {

  final Column[] columns;

  public RowDescription(Column[] columns) {
    this.columns = columns;
  }

  public Column[] getColumns() {
    return columns;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RowDescription that = (RowDescription) o;
    return Arrays.equals(columns, that.columns);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(columns);
  }


  @Override
  public String toString() {
    return "RowDescription{" +
      "columns=" + Arrays.toString(columns) +
      '}';
  }
}

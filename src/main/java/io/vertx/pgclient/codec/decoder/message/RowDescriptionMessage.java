package io.vertx.pgclient.codec.decoder.message;

import io.vertx.pgclient.codec.Message;
import io.vertx.pgclient.codec.decoder.Column;

import java.util.Arrays;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class RowDescriptionMessage implements Message {

  final Column[] columns;

  public RowDescriptionMessage(Column[] columns) {
    this.columns = columns;
  }

  public Column[] getColumns() {
    return columns;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RowDescriptionMessage that = (RowDescriptionMessage) o;
    return Arrays.equals(columns, that.columns);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(columns);
  }


  @Override
  public String toString() {
    return "RowDescriptionMessage{" +
      "columns=" + Arrays.toString(columns) +
      '}';
  }
}

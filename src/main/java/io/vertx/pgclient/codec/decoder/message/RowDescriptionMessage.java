package io.vertx.pgclient.codec.decoder.message;

import io.vertx.pgclient.codec.Message;
import io.vertx.pgclient.codec.decoder.Column;

public class RowDescriptionMessage implements Message {
  final Column[] columns;

  public RowDescriptionMessage(Column[] columns) {
    this.columns = columns;
  }

  public Column[] getColumns() {
    return columns;
  }
}

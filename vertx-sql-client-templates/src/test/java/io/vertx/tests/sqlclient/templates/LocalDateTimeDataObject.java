package io.vertx.tests.sqlclient.templates;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.sqlclient.templates.annotations.RowMapped;

import java.time.LocalDateTime;

@DataObject
@RowMapped
public class LocalDateTimeDataObject {

  private LocalDateTime localDateTime;

  public LocalDateTime getLocalDateTime() {
    return localDateTime;
  }

  public void setLocalDateTime(LocalDateTime localDateTime) {
    this.localDateTime = localDateTime;
  }
}

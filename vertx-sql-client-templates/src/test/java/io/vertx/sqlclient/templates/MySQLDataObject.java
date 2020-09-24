package io.vertx.sqlclient.templates;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.templates.annotations.ParametersMapped;
import io.vertx.sqlclient.templates.annotations.RowMapped;

import java.time.Duration;

@DataObject
@RowMapped
@ParametersMapped
public class MySQLDataObject {

  private Duration duration;

  public MySQLDataObject() {
  }

  public MySQLDataObject(JsonObject json) {
  }

  public Duration getDuration() {
    return duration;
  }

  public MySQLDataObject setDuration(Duration duration) {
    this.duration = duration;
    return this;
  }
}

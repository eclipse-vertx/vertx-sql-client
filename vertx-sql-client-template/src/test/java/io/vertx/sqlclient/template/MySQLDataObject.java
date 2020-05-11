package io.vertx.sqlclient.template;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.sqlclient.template.annotations.ParametersMapped;
import io.vertx.sqlclient.template.annotations.RowMapped;

import java.time.Duration;

@DataObject
@RowMapped
@ParametersMapped
public class MySQLDataObject {

  private Duration duration;

  public Duration getDuration() {
    return duration;
  }

  public MySQLDataObject setDuration(Duration duration) {
    this.duration = duration;
    return this;
  }
}

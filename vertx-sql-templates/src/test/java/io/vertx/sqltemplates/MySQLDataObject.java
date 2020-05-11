package io.vertx.sqltemplates;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.sqltemplates.annotations.ParametersMapped;
import io.vertx.sqltemplates.annotations.RowMapped;

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

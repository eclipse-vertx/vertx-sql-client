package io.vertx.mssqlclient.impl.protocol.datatype;

import java.time.LocalTime;

public class TimeNDataType extends MSSQLDataType {
  private byte scale;

  public TimeNDataType(byte scale) {
    super(MSSQLDataTypeId.TIMENTYPE_ID, LocalTime.class);
    this.scale = scale;
  }

  public byte scale() {
    return scale;
  }
}

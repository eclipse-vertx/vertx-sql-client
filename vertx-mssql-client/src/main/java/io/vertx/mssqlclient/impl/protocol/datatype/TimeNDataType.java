/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl.protocol.datatype;

import java.sql.JDBCType;
import java.time.LocalTime;

public class TimeNDataType extends MSSQLDataType {
  private byte scale;

  public TimeNDataType(byte scale) {
    super(MSSQLDataTypeId.TIMENTYPE_ID, LocalTime.class, JDBCType.TIME);
    this.scale = scale;
  }

  public byte scale() {
    return scale;
  }
}

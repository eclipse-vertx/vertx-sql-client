/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
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
import java.time.OffsetDateTime;

public class DateTimeOffsetNDataType extends MSSQLDataType {
  private final byte scale;

  public DateTimeOffsetNDataType(byte scale) {
    super(MSSQLDataTypeId.DATETIMEOFFSETNTYPE_ID, OffsetDateTime.class, JDBCType.TIMESTAMP_WITH_TIMEZONE);
    this.scale = scale;
  }

  public byte scale() {
    return scale;
  }
}

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
import java.time.LocalDateTime;

public class DateTime2NDataType extends MSSQLDataType {
  private final byte scale;

  public DateTime2NDataType(byte scale) {
    super(MSSQLDataTypeId.DATETIME2NTYPE_ID, LocalDateTime.class, JDBCType.TIMESTAMP);
    this.scale = scale;
  }

  public byte scale() {
    return scale;
  }
}

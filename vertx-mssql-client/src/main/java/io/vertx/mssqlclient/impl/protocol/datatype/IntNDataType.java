/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl.protocol.datatype;

import jdk.nashorn.internal.scripts.JD;

import java.sql.JDBCType;

/**
 * Variable-Length Data type, length may be 0x01, 0x02, 0x04, and 0x08.
 */
public class IntNDataType extends MSSQLDataType {
  public static final IntNDataType INT_1_DATA_TYPE = new IntNDataType(MSSQLDataTypeId.INTNTYPE_ID, Byte.class, 1, JDBCType.TINYINT);
  public static final IntNDataType INT_2_DATA_TYPE = new IntNDataType(MSSQLDataTypeId.INTNTYPE_ID, Short.class, 2, JDBCType.SMALLINT);
  public static final IntNDataType INT_4_DATA_TYPE = new IntNDataType(MSSQLDataTypeId.INTNTYPE_ID, Integer.class, 4, JDBCType.INTEGER);
  public static final IntNDataType INT_8_DATA_TYPE = new IntNDataType(MSSQLDataTypeId.INTNTYPE_ID, Long.class, 8, JDBCType.BIGINT);

  private final int length;

  private IntNDataType(int id, Class<?> mappedJavaType, int length, JDBCType jdbcType) {
    super(id, mappedJavaType, jdbcType);
    this.length = length;
  }

  public int length() {
    return length;
  }

  public static IntNDataType valueOf(int length) {
    switch (length) {
      case 1:
        return INT_1_DATA_TYPE;
      case 2:
        return INT_2_DATA_TYPE;
      case 4:
        return INT_4_DATA_TYPE;
      case 8:
        return INT_8_DATA_TYPE;
      default:
        throw new UnsupportedOperationException(String.format("SEVERE: Unsupported length=[%d] for decoding IntNDataType column metadata.", length));
    }
  }
}

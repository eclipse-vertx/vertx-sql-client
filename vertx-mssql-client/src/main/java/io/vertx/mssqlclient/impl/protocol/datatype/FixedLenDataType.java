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

import io.vertx.core.buffer.Buffer;

import java.sql.JDBCType;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class FixedLenDataType extends MSSQLDataType {
  public FixedLenDataType(int id, Class<?> mappedJavaType, JDBCType jdbcType) {
    super(id, mappedJavaType, jdbcType);
  }

  public static FixedLenDataType NULLTYPE = new FixedLenDataType(MSSQLDataTypeId.NULLTYPE_ID, null, JDBCType.OTHER);
  public static FixedLenDataType INT1TYPE = new FixedLenDataType(MSSQLDataTypeId.INT1TYPE_ID, Byte.class, JDBCType.TINYINT);
  public static FixedLenDataType BITTYPE = new FixedLenDataType(MSSQLDataTypeId.BITTYPE_ID, Buffer.class, JDBCType.BIT);
  public static FixedLenDataType INT2TYPE = new FixedLenDataType(MSSQLDataTypeId.INT2TYPE_ID, Short.class, JDBCType.SMALLINT);
  public static FixedLenDataType INT4TYPE = new FixedLenDataType(MSSQLDataTypeId.INT4TYPE_ID, Integer.class, JDBCType.INTEGER);
  public static FixedLenDataType DATETIM4TYPE = new FixedLenDataType(MSSQLDataTypeId.DATETIM4TYPE_ID, LocalDateTime.class, JDBCType.TIMESTAMP);
  public static FixedLenDataType FLT4TYPE = new FixedLenDataType(MSSQLDataTypeId.FLT4TYPE_ID, Float.class, JDBCType.REAL);
  public static FixedLenDataType MONEYTYPE = new FixedLenDataType(MSSQLDataTypeId.MONEYTYPE_ID, null, JDBCType.OTHER); //TODO
  public static FixedLenDataType DATETIMETYPE = new FixedLenDataType(MSSQLDataTypeId.DATETIMETYPE_ID, LocalDateTime.class, JDBCType.TIMESTAMP);
  public static FixedLenDataType FLT8TYPE = new FixedLenDataType(MSSQLDataTypeId.FLT8TYPE_ID, Double.class, JDBCType.DOUBLE);
  public static FixedLenDataType MONEY4TYPE = new FixedLenDataType(MSSQLDataTypeId.MONEY4TYPE_ID, null, JDBCType.OTHER); //TODO
  public static FixedLenDataType INT8TYPE = new FixedLenDataType(MSSQLDataTypeId.INT8TYPE_ID, Long.class, JDBCType.BIGINT);

  // DATENTYPE 0 or 3 length
  public static FixedLenDataType DATENTYPE = new FixedLenDataType(MSSQLDataTypeId.DATENTYPE_ID, LocalDate.class, JDBCType.DATE);
}

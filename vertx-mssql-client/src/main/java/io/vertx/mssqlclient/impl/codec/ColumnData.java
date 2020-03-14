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

package io.vertx.mssqlclient.impl.codec;

import io.vertx.mssqlclient.impl.protocol.datatype.MSSQLDataType;

public final class ColumnData {
  /*
    Protocol reference: https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-tds/58880b9f-381c-43b2-bf8b-0727a98c4f4c
   */
  private final long usertype;
  private final int flags;
  private final MSSQLDataType dataType;
  private final String colName;

  //  CryptoMetaData support?
  String tableName;

  public ColumnData(long usertype, int flags, MSSQLDataType dataType, String colName) {
    this.usertype = usertype;
    this.flags = flags;
    this.dataType = dataType;
    this.colName = colName;
  }

  public long usertype() {
    return usertype;
  }

  public int flags() {
    return flags;
  }

  public MSSQLDataType dataType() {
    return dataType;
  }

  public String colName() {
    return colName;
  }

  public String tableName() {
    return tableName;
  }


  public static final class Flags {
    public static final int NULLABLE = 0x0001;
    public static final int CASESEN = 0x0002;
    public static final int UPDATEABLE = 0x0004;
    public static final int IDENTITY = 0x0010;
    public static final int COMPUTED = 0x0020;
    // 2-BIT RESERVED for ODBC
    public static final int FIXED_LEN_CLR_TYPE = 0x0100;
    // 1-BIT RESERVED
    public static final int SPARSE_COLUMN_SET = 0x0400;
    public static final int ENCRYPTED = 0x0800;
    public static final int HIDDEN = 0x2000;
    public static final int KEY = 0x4000;
    public static final int NULLABLE_UNKNOWN = 0x8000;
  }
}

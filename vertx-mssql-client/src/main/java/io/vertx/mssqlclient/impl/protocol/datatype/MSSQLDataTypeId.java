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

public final class MSSQLDataTypeId {
  /*
    Fixed-Length Data Types
    https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-tds/859eb3d2-80d3-40f6-a637-414552c9c552
   */
  public static final int NULLTYPE_ID = 0x1F;
  public static final int INT1TYPE_ID = 0x30;
  public static final int BITTYPE_ID = 0x32;
  public static final int INT2TYPE_ID = 0x34;
  public static final int INT4TYPE_ID = 0x38;
  public static final int DATETIM4TYPE_ID = 0x3A;
  public static final int FLT4TYPE_ID = 0x3B;
  public static final int MONEYTYPE_ID = 0x3C;
  public static final int DATETIMETYPE_ID = 0x3D;
  public static final int FLT8TYPE_ID = 0x3E;
  public static final int MONEY4TYPE_ID = 0x7A;
  public static final int INT8TYPE_ID = 0x7F;

  /*
    Variable-Length Data Types
    https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-tds/ce3183a6-9d89-47e8-a02f-de5a1a1303de
   */
  public static final int GUIDTYPE_ID = 0x24;
  public static final int INTNTYPE_ID = 0x26;
  public static final int DECIMALTYPE_ID = 0x37;
  public static final int NUMERICTYPE_ID = 0x3F;
  public static final int BITNTYPE_ID = 0x68;
  public static final int DECIMALNTYPE_ID = 0x6A;
  public static final int NUMERICNTYPE_ID = 0x6C;
  public static final int FLTNTYPE_ID = 0x6D;
  public static final int MONEYNTYPE_ID = 0x6E;
  public static final int DATETIMNTYPE_ID = 0x6F;
  public static final int DATENTYPE_ID = 0x28;
  public static final int TIMENTYPE_ID = 0x29;
  public static final int DATETIME2NTYPE_ID = 0x2A;
  public static final int DATETIMEOFFSETNTYPE_ID = 0x2B;
  public static final int CHARTYPE_ID = 0x2F;
  public static final int VARCHARTYPE_ID = 0x27;
  public static final int BINARYTYPE_ID = 0x2D;
  public static final int VARBINARYTYPE_ID = 0x25;
  public static final int BIGVARBINTYPE_ID = 0xA5;
  public static final int BIGVARCHRTYPE_ID = 0xA7;
  public static final int BIGBINARYTYPE_ID = 0xAD;
  public static final int BIGCHARTYPE_ID = 0xAF;
  public static final int NVARCHARTYPE_ID = 0xE7;
  public static final int NCHARTYPE_ID = 0xEF;
  public static final int XMLTYPE_ID = 0xF1;
  public static final int UDTTYPE_ID = 0xF0;
  public static final int TEXTTYPE_ID = 0x23;
  public static final int IMAGETYPE_ID = 0x22;
  public static final int NTEXTTYPE_ID = 0x63;
  public static final int SSVARIANTTYPE_ID = 0x62;
}

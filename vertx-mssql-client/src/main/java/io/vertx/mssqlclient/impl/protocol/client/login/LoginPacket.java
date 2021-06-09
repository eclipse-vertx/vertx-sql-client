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

package io.vertx.mssqlclient.impl.protocol.client.login;

public final class LoginPacket {

  /*
    Protocol reference: https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-tds/773a62b6-ee89-4c02-9e5e-344882630aac
   */

  public static final int SQL_SERVER_2017_VERSION = 0x04000074;  // SQL_SERVER_2012, SQL_SERVER_2014, SQL_SERVER_2016, SQL_SERVER_2017

  public static final int DEFAULT_PACKET_SIZE = 4096;


  /*
    optionFlags1 = 8 BIT in Least Significant Bit Order
      fByteOrder(1 BIT) default: ORDER_X86
      fChar(1 BIT) default: CHARSET_ASCII
      fFloat(2 BIT) default: FLOAT_IEEE_754
      fDumpload(1 BIT) default: DUMPLOAD_ON
      fUseDB(1 BIT) default: USE_DB_OFF
      fDatabase(1 BIT) default: INIT_DB_WARN
      fSetLang(1 BIT) default: SET_LANG_OFF
   */
  public static final byte DEFAULT_OPTION_FLAGS1 = 0x00;
  public static final byte OPTION_FLAGS1_ORDER_X86 = 0x00;
  public static final byte OPTION_FLAGS1_ORDER_X68000 = 0x01;
  public static final byte OPTION_FLAGS1_CHARSET_ASCII = 0x00;
  public static final byte OPTION_FLAGS1_CHARSET_EBCDIC = 0x02;
  public static final byte OPTION_FLAGS1_FLOAT_IEEE_754 = 0x00;
  public static final byte OPTION_FALGS1_FLOAT_VAX = 0x04;
  public static final byte OPTION_FALGS1_ND5000 = 0x08;
  public static final byte OPTION_FLAGS1_DUMPLOAD_ON = 0x00;
  public static final byte OPTION_FLAGS1_DUMPLOAD_OFF = 0x10;
  public static final byte OPTION_FLAGS1_USE_DB_OFF = 0x00;
  public static final byte OPTION_FLAGS1_USE_DB_ON = 0x20;
  public static final byte OPTION_FLAGS1_INIT_DB_WARN = 0x00;
  public static final byte OPTION_FLAGS1_INIT_DB_FATAL = 0x40;
  public static final byte OPTION_FLAGS1_SET_LANG_OFF = 0x00;
  public static final byte OPTION_FLAGS1_SET_LANG_ON = (byte) 0x80;

  /*
    optionFlags2 = 8 BIT in Least Significant Bit Order
      fLanguage(1 BIT) default: LANG_WARN
      fODBC(1 BIT) default: ODBC_OFF
      fTranBoundary(1 BIT) default: FRESERVEDBIT
      fCacheConnect(1 BIT) default: FRESERVEDBIT
      fUserType(3 BIT) default: USER_NORMAL
      fIntSecurity(1 BIT) default: INTEGRATED_SECURITY_OFF
   */
  public static final byte DEFAULT_OPTION_FLAGS2 = 0x00;
  public static final byte OPTION_FLAGS2_INIT_LANG_WARN = 0x00;
  public static final byte OPTION_FLAGS2_INIT_LANG_FATAL = 0x01;
  public static final byte OPTION_FLAGS2_ODBC_OFF = 0x00;
  public static final byte OPTION_FLAGS2_ODBC_ON = 0x02;
  public static final byte OPTION_FLAGS2_USER_NORMAL = 0x00;
  public static final byte OPTION_FLAGS2_USER_SERVER = 0x10;
  public static final byte OPTION_FLAGS2_USER_REMUSER = 0x20;
  public static final byte OPTION_FLAGS2_USER_SQLREPL = 0x30;
  public static final byte OPTION_FLAGS2_INTEGRATED_SECURITY_OFF = 0x00;
  public static final byte OPTION_FLAGS2_INTEGRATED_SECURITY_ON = (byte) 0x80;

  /*
    typeFlags = 8 BIT in Least Significant Bit Order
      fSQLType(4 BIT) default: SQL_DFLT
      fOLEDB(1 BIT) default: OLEDB_OFF
      fReadOnlyIntent(1 BIT) default: 0
      2FRESERVEDBIT
   */
  public static final byte DEFAULT_TYPE_FLAGS = 0x00;
  public static final byte TYPE_FLAGS_SQL_DFLT = 0x00;
  public static final byte TYPE_FLAGS_SQL_TSQL = 0x01;
  public static final byte TYPE_FLAGS_OLEDB_OFF = 0x00;
  public static final byte TYPE_FLAGS_OLEDB_ON = 0x10;

  /*
    optionFlags3 = 8 BIT in Least Significant Bit Order
      fChangePassword(1 BIT)
      fUserInstance(1 BIT)
      fSendYukonBinaryXML(1 BIT)
      fUnknownCollationHandling(1 BIT)
      fExtension(1 BIT)
      3FRESERVEDBIT
   */
  public static final byte DEFAULT_OPTION_FLAGS3 = 0x00;


  private byte typeFlags = 0x00;

  private long timezone = 0; // LONG 4 BYTE

  private long clientLCID = 0; // LONG 4 BYTE

  // OffsetLength

}

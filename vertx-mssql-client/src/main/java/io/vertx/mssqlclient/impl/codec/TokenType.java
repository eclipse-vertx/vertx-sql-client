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

package io.vertx.mssqlclient.impl.codec;

@SuppressWarnings("unused")
public class TokenType {

  public static final short ALTMETADATA = 0x88;
  public static final short ALTROW = 0xD3;
  public static final short COLMETADATA = 0x81;
  public static final short COLINFO = 0xA5;
  public static final short DONE = 0xFD;
  public static final short DONEPROC = 0xFE;
  public static final short DONEINPROC = 0xFF;
  public static final short ENVCHANGE = 0xE3;
  public static final short ERROR = 0xAA;
  public static final short FEATUREEXTACK = 0xAE;
  public static final short FEDAUTHINFO = 0xEE;
  public static final short INFO = 0xAB;
  public static final short LOGINACK = 0xAD;
  public static final short NBCROW = 0xD2;
  public static final short ORDER = 0xA9;
  public static final short RETURNSTATUS = 0x79;
  public static final short RETURNVALUE = 0xAC;
  public static final short ROW = 0xD1;
  public static final short SESSIONSTATE = 0xE4;
  public static final short SSPI = 0xED;
  public static final short TABNAME = 0xA4;
  public static final short OFFSET = 0x78;

  private TokenType() {
    // Constants class
  }
}

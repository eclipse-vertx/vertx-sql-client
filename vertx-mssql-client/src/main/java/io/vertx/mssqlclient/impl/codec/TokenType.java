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

  public static final int ALTMETADATA = 0x88;
  public static final int ALTROW = 0xD3;
  public static final int COLMETADATA = 0x81;
  public static final int COLINFO = 0xA5;
  public static final int DONE = 0xFD;
  public static final int DONEPROC = 0xFE;
  public static final int DONEINPROC = 0xFF;
  public static final int ENVCHANGE = 0xE3;
  public static final int ERROR = 0xAA;
  public static final int FEATUREEXTACK = 0xAE;
  public static final int FEDAUTHINFO = 0xEE;
  public static final int INFO = 0xAB;
  public static final int LOGINACK = 0xAD;
  public static final int NBCROW = 0xD2;
  public static final int ORDER = 0xA9;
  public static final int RETURNSTATUS = 0x79;
  public static final int RETURNVALUE = 0xAC;
  public static final int ROW = 0xD1;
  public static final int SESSIONSTATE = 0xE4;
  public static final int SSPI = 0xED;
  public static final int TABNAME = 0xA4;
  public static final int OFFSET = 0x78;

  private TokenType() {
    // Constants class
  }
}

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
public class EnvChange {

  public static final short DATABASE = 1;
  public static final short LANGUAGE = 2;
  public static final short CHARSET = 3;
  public static final short PACKETSIZE = 4;
  public static final short SORTLOCALEID = 5;
  public static final short SORTFLAGS = 6;
  public static final short SQLCOLLATION = 7;
  public static final short XACT_BEGIN = 8;
  public static final short XACT_COMMIT = 9;
  public static final short XACT_ROLLBACK = 10;
  public static final short DTC_ENLIST = 11;
  public static final short DTC_DEFECT = 12;
  public static final short CHANGE_MIRROR = 13;
  public static final short UNUSED_14 = 14;
  public static final short DTC_PROMOTE = 15;
  public static final short DTC_MGR_ADDR = 16;
  public static final short XACT_ENDED = 17;
  public static final short RESET_COMPLETE = 18;
  public static final short USER_INFO = 19;
  public static final short ROUTING = 20;

  private EnvChange() {
    // Constants
  }
}

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

package io.vertx.mssqlclient.impl.protocol;

public class EnvChange {

  public static final int DATABASE = 1;
  public static final int LANGUAGE = 2;
  public static final int CHARSET = 3;
  public static final int PACKETSIZE = 4;
  public static final int SORTLOCALEID = 5;
  public static final int SORTFLAGS = 6;
  public static final int SQLCOLLATION = 7;
  public static final int XACT_BEGIN = 8;
  public static final int XACT_COMMIT = 9;
  public static final int XACT_ROLLBACK = 10;
  public static final int DTC_ENLIST = 11;
  public static final int DTC_DEFECT = 12;
  public static final int CHANGE_MIRROR = 13;
  public static final int UNUSED_14 = 14;
  public static final int DTC_PROMOTE = 15;
  public static final int DTC_MGR_ADDR = 16;
  public static final int XACT_ENDED = 17;
  public static final int RESET_COMPLETE = 18;
  public static final int USER_INFO = 19;
  public static final int ROUTING = 20;

  private EnvChange() {
    // Constants
  }
}

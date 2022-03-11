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

package io.vertx.oracleclient.impl;

import io.vertx.oracleclient.OracleException;
import oracle.jdbc.OracleDatabaseException;

import java.sql.SQLException;

public class FailureUtil {

  public static Throwable sanitize(Throwable t) {
    if (t instanceof SQLException) {
      SQLException se = (SQLException) t;
      Throwable cause = se.getCause();
      if (cause instanceof OracleDatabaseException) {
        OracleDatabaseException oae = (OracleDatabaseException) cause;
        return new OracleException(oae.toString(), true);
      }
    }
    return t;
  }

  private FailureUtil() {
    // Utility
  }
}

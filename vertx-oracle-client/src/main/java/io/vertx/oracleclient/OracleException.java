/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.oracleclient;

import io.vertx.sqlclient.DatabaseException;

import java.sql.SQLException;

/**
 * The {@link DatabaseException} for Oracle.
 */
public class OracleException extends DatabaseException {

  public OracleException(String message, int errorCode, String sqlState) {
    super(message, errorCode, sqlState);
  }

  public OracleException(SQLException e) {
    super(e.getMessage(), e.getErrorCode(), e.getSQLState(), e);
  }
}

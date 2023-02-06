/*
 * Copyright (c) 2011-2023 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.sqlclient;

import io.vertx.core.VertxException;

/**
 * Base class for SQL Client database access failures.
 */
public abstract class DatabaseException extends VertxException {

  private final int errorCode;
  private final String sqlState;

  protected DatabaseException(String message, int errorCode, String sqlState) {
    super(message, true);
    this.errorCode = errorCode;
    this.sqlState = sqlState;
  }

  /**
   * Database specific error code.
   *
   * @return the error code
   */
  public int getErrorCode() {
    return errorCode;
  }

  /**
   * SQL State (XOPEN or SQL:2003 conventions).
   *
   * @return the SQL state if known or {@code null}
   */
  public String getSqlState() {
    return sqlState;
  }
}

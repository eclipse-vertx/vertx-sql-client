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

package io.vertx.mysqlclient;

/**
 * A {@link RuntimeException} signals that an error occurred.
 */
public class MySQLException extends RuntimeException {
  private final int errorCode;
  private final String sqlState;

  public MySQLException(String message, int errorCode, String sqlState) {
    super(message);
    this.errorCode = errorCode;
    this.sqlState = sqlState;
  }

  /**
   * Get the error code in the error message sent from MySQL server.
   *
   * @return the error code
   */
  public int getErrorCode() {
    return errorCode;
  }

  /**
   * Get the SQL state in the error message sent from MySQL server.
   *
   * @return the SQL state
   */
  public String getSqlState() {
    return sqlState;
  }

  /**
   * Get the error message in the error message sent from MySQL server.
   *
   * @return the error message
   */
  @Override
  public String getMessage() {
    return super.getMessage();
  }
}

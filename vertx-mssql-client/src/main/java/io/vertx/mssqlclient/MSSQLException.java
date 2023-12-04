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

package io.vertx.mssqlclient;

import io.vertx.sqlclient.DatabaseException;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link DatabaseException} for MS SQL Server.
 */
public class MSSQLException extends DatabaseException {

  private final byte state;
  private final byte severity;
  private final String errorMessage;
  private final String serverName;
  private final String procedureName;
  private final int lineNumber;
  private List<MSSQLException> additional;

  public MSSQLException(int number, byte state, byte severity, String errorMessage, String serverName, String procedureName, int lineNumber) {
    super(formatMessage(number, state, severity, errorMessage, serverName, procedureName, lineNumber), number, generateStateCode(number, state));
    this.state = state;
    this.severity = severity;
    this.errorMessage = errorMessage;
    this.serverName = serverName;
    this.procedureName = procedureName;
    this.lineNumber = lineNumber;
  }


  /**
   * Generates the JDBC state code based on the error number returned from the database.
   *
   * This method is derived from the method with the same name in the JDBC driver
   * in com.microsoft.sqlserver.jdbc.SQLServerException
   *
   * @param errNum
   *        the vendor error number
   * @param databaseState
   *        the database state
   * @return the SQL state code (XOPEN or SQL:2003 conventions)
   */
  static String generateStateCode(int errNum, int databaseState) {
      switch (errNum) {
        // case 18456: return EXCEPTION_XOPEN_CONNECTION_CANT_ESTABLISH; //username password wrong at login
        case 8152:
          return "22001"; // String data right truncation
        case 515: // 2.2705
        case 547:
        case 2601:
        case 2627:
          return "23000"; // Integrity constraint violation
        case 2714:
          return "S0001"; // table already exists
        case 208:
          return "S0002"; // table not found
        case 1205:
          return "40001"; // deadlock detected
        default: {
          return "S" + String.format( "%4s", databaseState ).replaceAll( " ", "0" );
        }
      }
  }

  public void add(MSSQLException e) {
    if (additional == null) {
      additional = new ArrayList<>(3);
    }
    additional.add(e);
  }

  /**
   * @deprecated use {@link #getErrorCode()} instead
   */
  @Deprecated
  public int number() {
    return getErrorCode();
  }

  public byte state() {
    return state;
  }

  public byte severity() {
    return severity;
  }

  public String errorMessage() {
    return errorMessage;
  }

  public String serverName() {
    return serverName;
  }

  public String procedureName() {
    return procedureName;
  }

  public int lineNumber() {
    return lineNumber;
  }

  /**
   * @return additional errors reported by the client, or {@code null}
   */
  public List<MSSQLException> additional() {
    return additional;
  }

  private static String formatMessage(int number, byte state, byte severity, String errorMessage, String serverName, String procedureName, int lineNumber) {
    StringBuilder sb = new StringBuilder("{")
      .append("number=").append(number)
      .append(", state=").append(state)
      .append(", severity=").append(severity);
    if (errorMessage != null && !errorMessage.isEmpty()) {
      sb.append(", message='").append(errorMessage).append('\'');
    }
    if (serverName != null && !serverName.isEmpty()) {
      sb.append(", serverName='").append(serverName).append('\'');
    }
    if (procedureName != null && !procedureName.isEmpty()) {
      sb.append(", procedureName='").append(procedureName).append('\'');
    }
    sb.append(", lineNumber=").append(lineNumber);
    return sb.append('}').toString();
  }
}

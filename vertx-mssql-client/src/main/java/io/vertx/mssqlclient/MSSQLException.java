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
    super(formatMessage(number, state, severity, errorMessage, serverName, procedureName, lineNumber), number, null);
    this.state = state;
    this.severity = severity;
    this.errorMessage = errorMessage;
    this.serverName = serverName;
    this.procedureName = procedureName;
    this.lineNumber = lineNumber;
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

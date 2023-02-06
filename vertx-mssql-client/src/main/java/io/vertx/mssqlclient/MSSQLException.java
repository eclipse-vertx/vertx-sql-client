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

  private final int number;
  private final byte state;
  private final byte severity;
  private final String message;
  private final String serverName;
  private final String procedureName;
  private final int lineNumber;
  private List<MSSQLException> additional;

  public MSSQLException(int number, byte state, byte severity, String message, String serverName, String procedureName, int lineNumber) {
    super(null, number, null);
    this.number = number;
    this.state = state;
    this.severity = severity;
    this.message = message;
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

  public int number() {
    return number;
  }

  public byte state() {
    return state;
  }

  public byte severity() {
    return severity;
  }

  public String errorMessage() {
    return message;
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

  @Override
  public String getMessage() {
    StringBuilder sb = new StringBuilder("{")
      .append("number=").append(number)
      .append(", state=").append(state)
      .append(", severity=").append(severity);
    if (message != null && !message.isEmpty()) {
      sb.append(", message='").append(message).append('\'');
    }
    if (serverName != null && !serverName.isEmpty()) {
      sb.append(", serverName='").append(serverName).append('\'');
    }
    if (procedureName != null && !procedureName.isEmpty()) {
      sb.append(", procedureName='").append(procedureName).append('\'');
    }
    sb.append(", lineNumber=").append(lineNumber);
    if (additional != null) {
      sb.append(", additional=").append(additional);
    }
    return sb.append('}').toString();
  }
}

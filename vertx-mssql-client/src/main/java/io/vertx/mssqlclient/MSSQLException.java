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

package io.vertx.mssqlclient;

/**
 * A {@link RuntimeException} signals that an error occurred.
 */
public class MSSQLException extends RuntimeException {
  private final int number;
  private final byte state;
  private final byte severity;
  private final String serverName;
  private final String procedureName;
  private final int lineNumber;

  public MSSQLException(int number, byte state, byte severity, String message, String serverName, String procedureName, int lineNumber) {
    super(message);
    this.number = number;
    this.state = state;
    this.severity = severity;
    this.serverName = serverName;
    this.procedureName = procedureName;
    this.lineNumber = lineNumber;
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

  public String serverName() {
    return serverName;
  }

  public String procedureName() {
    return procedureName;
  }

  public int lineNumber() {
    return lineNumber;
  }
}

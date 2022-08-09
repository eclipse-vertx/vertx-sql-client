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

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * An information message sent by the server to the client.
 */
@DataObject(generateConverter = true)
public class MSSQLInfo {

  private int number;
  private byte state;
  private byte severity;
  private String message;
  private String serverName;
  private String procedureName;
  private int lineNumber;

  public MSSQLInfo() {
  }

  public MSSQLInfo(MSSQLInfo other) {
    this.number = other.number;
    this.state = other.state;
    this.severity = other.severity;
    this.message = other.message;
    this.serverName = other.serverName;
    this.procedureName = other.procedureName;
    this.lineNumber = other.lineNumber;
  }

  public MSSQLInfo(JsonObject json) {
    MSSQLInfoConverter.fromJson(json, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    MSSQLInfoConverter.toJson(this, json);
    return json;
  }

  public int getNumber() {
    return number;
  }

  public MSSQLInfo setNumber(int number) {
    this.number = number;
    return this;
  }

  public byte getState() {
    return state;
  }

  public MSSQLInfo setState(byte state) {
    this.state = state;
    return this;
  }

  public byte getSeverity() {
    return severity;
  }

  public MSSQLInfo setSeverity(byte severity) {
    this.severity = severity;
    return this;
  }

  public String getMessage() {
    return message;
  }

  public MSSQLInfo setMessage(String message) {
    this.message = message;
    return this;
  }

  public String getServerName() {
    return serverName;
  }

  public MSSQLInfo setServerName(String serverName) {
    this.serverName = serverName;
    return this;
  }

  public String getProcedureName() {
    return procedureName;
  }

  public MSSQLInfo setProcedureName(String procedureName) {
    this.procedureName = procedureName;
    return this;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public MSSQLInfo setLineNumber(int lineNumber) {
    this.lineNumber = lineNumber;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("MSSQL Info {")
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
    return sb.append('}').toString();
  }
}

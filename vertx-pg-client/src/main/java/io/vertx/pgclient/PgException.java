/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.vertx.pgclient;

import io.vertx.core.json.Json;
import io.vertx.pgclient.impl.codec.Response;

/**
 * PostgreSQL error including all <a href="https://www.postgresql.org/docs/current/protocol-error-fields.html">fields
 * of the ErrorResponse message</a> of the PostgreSQL frontend/backend protocol.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgException extends RuntimeException {

  private final Response response;
  private final String errorMessage;
  private final String severity;
  private final String code;
  private final String detail;

  public PgException(Response response) {
    this.response = response;
    this.errorMessage = null;
    this.severity = null;
    this.code = null;
    this.detail = null;
  }

  public PgException(String errorMessage, String severity, String code, String detail) {
    this.response = null;
    this.errorMessage = errorMessage;
    this.severity = severity;
    this.code = code;
    this.detail = detail;
  }

  /**
   * @return the primary human-readable error message
   *     (<a href="https://www.postgresql.org/docs/current/protocol-error-fields.html">'M' field</a>)
   */
  public String getErrorMessage() {
    // getErrorMessage() avoids name clash with RuntimeException#getMessage()
    return response == null ? errorMessage : response.getMessage();
  }

  /**
   * @return the severity: ERROR, FATAL, or PANIC, or a localized translation of one of these
   *     (<a href="https://www.postgresql.org/docs/current/protocol-error-fields.html">'S' field</a>)
   */
  public String getSeverity() {
    return response == null ? severity : response.getSeverity();
  }

  /**
   * @return the SQLSTATE code for the error
   *     (<a href="https://www.postgresql.org/docs/current/protocol-error-fields.html">'S' field</a>,
   *     <a href="https://www.postgresql.org/docs/current/errcodes-appendix.html">value list</a>),
   *     it is never localized
   */
  public String getCode() {
    return response == null ? code : response.getCode();
  }

  /**
   * @return an optional secondary error message carrying more detail about the problem
   *     (<a href="https://www.postgresql.org/docs/current/protocol-error-fields.html">'D' field</a>).
   *     a newline indicates paragraph break.
   */
  public String getDetail() {
    return response == null ? detail : response.getDetail();
  }

  public String getHint() {
    return response == null ? null : response.getHint();
  }

  public String getPosition() {
    return response == null ? null : response.getPosition();
  }

  public String getWhere() {
    return response == null ? null : response.getWhere();
  }

  public String getFile() {
    return response == null ? null : response.getFile();
  }

  public String getLine() {
    return response == null ? null : response.getLine();
  }

  public String getRoutine() {
    return response == null ? null : response.getRoutine();
  }

  public String getSchema() {
    return response == null ? null : response.getSchema();
  }

  public String getTable() {
    return response == null ? null : response.getTable();
  }

  public String getColumn() {
    return response == null ? null : response.getColumn();
  }

  public String getDataType() {
    return response == null ? null : response.getDataType();
  }

  public String getConstraint() {
    return response == null ? null : response.getConstraint();
  }

  public String getInternalPosition() {
    return response == null ? null : response.getInternalPosition();
  }

  public String getInternalQuery() {
    return response == null ? null : response.getInternalQuery();
  }

  private static void append(StringBuffer stringBuffer, String key, String value) {
    if (value != null) {
      stringBuffer.append(", \"").append(key).append("\": ").append(Json.encode(value));
    }
  }

  /**
   * A serialized JsonObject of all non-null error message fields.
   */
  @Override
  public String getMessage() {
    StringBuffer sb = new StringBuffer();
    append(sb, "message", getErrorMessage());
    append(sb, "severity", getSeverity());
    append(sb, "code", getCode());
    append(sb, "detail", getDetail());
    append(sb, "hint", getHint());
    append(sb, "position", getPosition());
    append(sb, "internalPosition", getInternalPosition());
    append(sb, "internalQuery", getInternalQuery());
    append(sb, "where", getWhere());
    append(sb, "file", getFile());
    append(sb, "line", getLine());
    append(sb, "routine", getRoutine());
    append(sb, "schema", getSchema());
    append(sb, "table", getTable());
    append(sb, "column", getColumn());
    append(sb, "dataType", getDataType());
    append(sb, "constraint", getConstraint());
    if (sb.length() == 0) {
      return "{}";
    }
    sb.append(" }");
    sb.setCharAt(0, '{');  // replace leading comma
    return sb.toString();
  }
}

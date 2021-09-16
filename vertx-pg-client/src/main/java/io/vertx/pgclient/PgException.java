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

/**
 * PostgreSQL error including all <a href="https://www.postgresql.org/docs/current/protocol-error-fields.html">fields
 * of the ErrorResponse message</a> of the PostgreSQL frontend/backend protocol.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgException extends RuntimeException {

  private static String formatMessage(String errorMessage, String severity, String code) {
    StringBuilder sb = new StringBuilder();
    if (severity != null) {
      sb.append(severity).append(":");
    }
    if (errorMessage != null) {
      if (sb.length() > 0) {
        sb.append(' ');
      }
      sb.append(errorMessage);
    }
    if (code != null) {
      if (sb.length() > 0) {
        sb.append(' ');
      }
      sb.append('(').append(code).append(')');
    }
    return sb.toString();
  }

  private final String errorMessage;
  private final String severity;
  private final String code;
  private final String detail;
  private final String hint;
  private final String position;
  private final String internalPosition;
  private final String internalQuery;
  private final String where;
  private final String file;
  private final String line;
  private final String routine;
  private final String schema;
  private final String table;
  private final String column;
  private final String dataType;
  private final String constraint;

  public PgException(String errorMessage, String severity, String code, String detail) {
    super(formatMessage(errorMessage, severity, code));
    this.errorMessage = errorMessage;
    this.severity = severity;
    this.code = code;
    this.detail = detail;
    this.hint = null;
    this.position = null;
    this.internalPosition = null;
    this.internalQuery = null;
    this.where = null;
    this.file = null;
    this.line = null;
    this.routine = null;
    this.schema = null;
    this.table = null;
    this.column = null;
    this.dataType = null;
    this.constraint = null;
  }

  public PgException(String errorMessage, String severity, String code, String detail, String hint, String position,
                     String internalPosition, String internalQuery, String where, String file, String line, String routine,
                     String schema, String table, String column, String dataType, String constraint) {
    super(formatMessage(errorMessage, severity, code));
    this.errorMessage = errorMessage;
    this.severity = severity;
    this.code = code;
    this.detail = detail;
    this.hint = hint;
    this.position = position;
    this.internalPosition = internalPosition;
    this.internalQuery = internalQuery;
    this.where = where;
    this.file = file;
    this.line = line;
    this.routine = routine;
    this.schema = schema;
    this.table = table;
    this.column = column;
    this.dataType = dataType;
    this.constraint = constraint;
  }

  /**
   * @return the primary human-readable error message
   * (<a href="https://www.postgresql.org/docs/current/protocol-error-fields.html">'M' field</a>)
   */
  public String getErrorMessage() {
    // getErrorMessage() avoids name clash with RuntimeException#getMessage()
    return errorMessage;
  }

  /**
   * @return the severity: ERROR, FATAL, or PANIC, or a localized translation of one of these
   * (<a href="https://www.postgresql.org/docs/current/protocol-error-fields.html">'S' field</a>)
   */
  public String getSeverity() {
    return severity;
  }

  /**
   * @return the SQLSTATE code for the error
   * (<a href="https://www.postgresql.org/docs/current/protocol-error-fields.html">'S' field</a>,
   * <a href="https://www.postgresql.org/docs/current/errcodes-appendix.html">value list</a>),
   * it is never localized
   */
  public String getCode() {
    return code;
  }

  /**
   * @return an optional secondary error message carrying more detail about the problem
   * (<a href="https://www.postgresql.org/docs/current/protocol-error-fields.html">'D' field</a>),
   * a newline indicates paragraph break.
   */
  public String getDetail() {
    return detail;
  }

  /**
   * @return an optional suggestion (advice) what to do about the problem
   * (<a href="https://www.postgresql.org/docs/current/protocol-error-fields.html">'H' field</a>),
   * a newline indicates paragraph break.
   */
  public String getHint() {
    return hint;
  }

  /**
   * @return a decimal ASCII integer, indicating an error cursor position as an index into the original
   * query string. (<a href="https://www.postgresql.org/docs/current/protocol-error-fields.html">'P' field</a>).
   * The first character has index 1, and positions are measured in characters not bytes.
   */
  public String getPosition() {
    return position;
  }

  /**
   * @return an indication of the context in which the error occurred
   * (<a href="https://www.postgresql.org/docs/current/protocol-error-fields.html">'W' field</a>).
   * Presently this includes a call stack traceback of active procedural language functions and
   * internally-generated queries. The trace is one entry per line, most recent first.
   */
  public String getWhere() {
    return where;
  }

  /**
   * @return file name of the source-code location where the error was reported
   * (<a href="https://www.postgresql.org/docs/current/protocol-error-fields.html">'F' field</a>).
   */
  public String getFile() {
    return file;
  }

  /**
   * @return line number of the source-code location where the error was reported
   * (<a href="https://www.postgresql.org/docs/current/protocol-error-fields.html">'L' field</a>).
   */
  public String getLine() {
    return line;
  }

  /**
   * @return name of the source-code routine reporting the error
   * (<a href="https://www.postgresql.org/docs/current/protocol-error-fields.html">'R' field</a>).
   */
  public String getRoutine() {
    return routine;
  }

  /**
   * @return if the error was associated with a specific database object, the name of the schema containing
   * that object, if any
   * (<a href="https://www.postgresql.org/docs/current/protocol-error-fields.html">'s' field</a>).
   */
  public String getSchema() {
    return schema;
  }

  /**
   * @return if the error was associated with a specific table, the name of the table
   * (<a href="https://www.postgresql.org/docs/current/protocol-error-fields.html">'t' field</a>).
   */
  public String getTable() {
    return table;
  }

  /**
   * @return if the error was associated with a specific table column, the name of the column
   * (<a href="https://www.postgresql.org/docs/current/protocol-error-fields.html">'c' field</a>).
   */
  public String getColumn() {
    return column;
  }

  /**
   * @return if the error was associated with a specific data type, the name of the data type
   * (<a href="https://www.postgresql.org/docs/current/protocol-error-fields.html">'d' field</a>).
   */
  public String getDataType() {
    return dataType;
  }

  /**
   * @return if the error was associated with a specific constraint, the name of the constraint
   * (<a href="https://www.postgresql.org/docs/current/protocol-error-fields.html">'n' field</a>).
   */
  public String getConstraint() {
    return constraint;
  }

  /**
   * @return a decimal ASCII integer, indicating an error cursor position
   * (<a href="https://www.postgresql.org/docs/current/protocol-error-fields.html">'p' field</a>)
   * as an index into the internally generated command (see 'q' field).
   */
  public String getInternalPosition() {
    return internalPosition;
  }

  /**
   * @return the text of a failed internally-generated command
   * (<a href="https://www.postgresql.org/docs/current/protocol-error-fields.html">'q' field</a>).
   */
  public String getInternalQuery() {
    return internalQuery;
  }

  private static void append(StringBuffer stringBuffer, String key, String value) {
    if (value != null) {
      stringBuffer.append(", \"").append(key).append("\": ").append(Json.encode(value));
    }
  }
}

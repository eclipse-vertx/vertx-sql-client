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

package io.reactiverse.pgclient.impl.pg;

/**
 *
 * <p>
 * A common response message for PostgreSQL
 * <a href="https://www.postgresql.org/docs/9.5/static/protocol-error-fields.html">Error and Notice Message Fields</a>
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

abstract class Response {

  private String severity;
  private String code;
  private String message;
  private String detail;
  private String hint;
  private String position;
  private String internalPosition;
  private String internalQuery;
  private String where;
  private String file;
  private String line;
  private String routine;
  private String schema;
  private String table;
  private String column;
  private String dataType;
  private String constraint;

  String getSeverity() {
    return severity;
  }

  void setSeverity(String severity) {
    this.severity = severity;
  }

  String getCode() {
    return code;
  }

  void setCode(String code) {
    this.code = code;
  }

  String getMessage() {
    return message;
  }

  void setMessage(String message) {
    this.message = message;
  }

  String getDetail() {
    return detail;
  }

  void setDetail(String detail) {
    this.detail = detail;
  }

  String getHint() {
    return hint;
  }

  void setHint(String hint) {
    this.hint = hint;
  }

  String getPosition() {
    return position;
  }

  void setPosition(String position) {
    this.position = position;
  }

  String getWhere() {
    return where;
  }

  void setWhere(String where) {
    this.where = where;
  }

  String getFile() {
    return file;
  }

  void setFile(String file) {
    this.file = file;
  }

  String getLine() {
    return line;
  }

  void setLine(String line) {
    this.line = line;
  }

  String getRoutine() {
    return routine;
  }

  void setRoutine(String routine) {
    this.routine = routine;
  }

  String getSchema() {
    return schema;
  }

  void setSchema(String schema) {
    this.schema = schema;
  }

  String getTable() {
    return table;
  }

  void setTable(String table) {
    this.table = table;
  }

  String getColumn() {
    return column;
  }

  void setColumn(String column) {
    this.column = column;
  }

  String getDataType() {
    return dataType;
  }

  void setDataType(String dataType) {
    this.dataType = dataType;
  }

  String getConstraint() {
    return constraint;
  }

  void setConstraint(String constraint) {
    this.constraint = constraint;
  }


  String getInternalPosition() {
    return internalPosition;
  }

  void setInternalPosition(String internalPosition) {
    this.internalPosition = internalPosition;
  }

  String getInternalQuery() {
    return internalQuery;
  }

  void setInternalQuery(String internalQuery) {
    this.internalQuery = internalQuery;
  }


  @Override
  public String toString() {
    return "Response{" +
      "severity='" + severity + '\'' +
      ", code='" + code + '\'' +
      ", message='" + message + '\'' +
      ", detail='" + detail + '\'' +
      ", hint='" + hint + '\'' +
      ", position='" + position + '\'' +
      ", internalPosition='" + internalPosition + '\'' +
      ", internalQuery='" + internalQuery + '\'' +
      ", where='" + where + '\'' +
      ", file='" + file + '\'' +
      ", line='" + line + '\'' +
      ", routine='" + routine + '\'' +
      ", schema='" + schema + '\'' +
      ", table='" + table + '\'' +
      ", column='" + column + '\'' +
      ", dataType='" + dataType + '\'' +
      ", constraint='" + constraint + '\'' +
      '}';
  }
}

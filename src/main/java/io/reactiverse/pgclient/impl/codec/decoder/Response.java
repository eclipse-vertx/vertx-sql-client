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

package io.reactiverse.pgclient.impl.codec.decoder;


import java.util.Objects;

/**
 *
 * <p>
 * A common response message for PostgreSQL
 * <a href="https://www.postgresql.org/docs/9.5/static/protocol-error-fields.html">Error and Notice Message Fields</a>
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public abstract class Response {

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

  public String getSeverity() {
    return severity;
  }

  public void setSeverity(String severity) {
    this.severity = severity;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getDetail() {
    return detail;
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }

  public String getHint() {
    return hint;
  }

  public void setHint(String hint) {
    this.hint = hint;
  }

  public String getPosition() {
    return position;
  }

  public void setPosition(String position) {
    this.position = position;
  }

  public String getWhere() {
    return where;
  }

  public void setWhere(String where) {
    this.where = where;
  }

  public String getFile() {
    return file;
  }

  public void setFile(String file) {
    this.file = file;
  }

  public String getLine() {
    return line;
  }

  public void setLine(String line) {
    this.line = line;
  }

  public String getRoutine() {
    return routine;
  }

  public void setRoutine(String routine) {
    this.routine = routine;
  }

  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public String getTable() {
    return table;
  }

  public void setTable(String table) {
    this.table = table;
  }

  public String getColumn() {
    return column;
  }

  public void setColumn(String column) {
    this.column = column;
  }

  public String getDataType() {
    return dataType;
  }

  public void setDataType(String dataType) {
    this.dataType = dataType;
  }

  public String getConstraint() {
    return constraint;
  }

  public void setConstraint(String constraint) {
    this.constraint = constraint;
  }


  public String getInternalPosition() {
    return internalPosition;
  }

  public void setInternalPosition(String internalPosition) {
    this.internalPosition = internalPosition;
  }

  public String getInternalQuery() {
    return internalQuery;
  }

  public void setInternalQuery(String internalQuery) {
    this.internalQuery = internalQuery;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Response that = (Response) o;
    return Objects.equals(severity, that.severity) &&
      Objects.equals(code, that.code) &&
      Objects.equals(message, that.message) &&
      Objects.equals(detail, that.detail) &&
      Objects.equals(hint, that.hint) &&
      Objects.equals(position, that.position) &&
      Objects.equals(internalPosition, that.internalPosition) &&
      Objects.equals(internalQuery, that.internalQuery) &&
      Objects.equals(where, that.where) &&
      Objects.equals(file, that.file) &&
      Objects.equals(line, that.line) &&
      Objects.equals(routine, that.routine) &&
      Objects.equals(schema, that.schema) &&
      Objects.equals(table, that.table) &&
      Objects.equals(column, that.column) &&
      Objects.equals(dataType, that.dataType) &&
      Objects.equals(constraint, that.constraint);
  }

  @Override
  public int hashCode() {
    return Objects.hash(severity, code, message, detail, hint, position, internalPosition, internalQuery, where, file,
      line, routine, schema, table, column, dataType, constraint);
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

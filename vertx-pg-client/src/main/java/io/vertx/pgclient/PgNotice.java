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

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.json.JsonObject;

/**
 * A notification emited by Postgres.
 */
@DataObject(generateConverter = true)
public class PgNotice {

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

  public PgNotice() {
  }

  public PgNotice(JsonObject json) {
    PgNoticeConverter.fromJson(json, this);
  }

  public String getSeverity() {
    return severity;
  }

  public PgNotice setSeverity(String severity) {
    this.severity = severity;
    return this;
  }

  public String getCode() {
    return code;
  }

  public PgNotice setCode(String code) {
    this.code = code;
    return this;
  }

  public String getMessage() {
    return message;
  }

  public PgNotice setMessage(String message) {
    this.message = message;
    return this;
  }

  public String getDetail() {
    return detail;
  }

  public PgNotice setDetail(String detail) {
    this.detail = detail;
    return this;
  }

  public String getHint() {
    return hint;
  }

  public PgNotice setHint(String hint) {
    this.hint = hint;
    return this;
  }

  public String getPosition() {
    return position;
  }

  public PgNotice setPosition(String position) {
    this.position = position;
    return this;
  }

  public String getInternalPosition() {
    return internalPosition;
  }

  public PgNotice setInternalPosition(String internalPosition) {
    this.internalPosition = internalPosition;
    return this;
  }

  public String getInternalQuery() {
    return internalQuery;
  }

  public PgNotice setInternalQuery(String internalQuery) {
    this.internalQuery = internalQuery;
    return this;
  }

  public String getWhere() {
    return where;
  }

  public PgNotice setWhere(String where) {
    this.where = where;
    return this;
  }

  public String getFile() {
    return file;
  }

  public PgNotice setFile(String file) {
    this.file = file;
    return this;
  }

  public String getLine() {
    return line;
  }

  public PgNotice setLine(String line) {
    this.line = line;
    return this;
  }

  public String getRoutine() {
    return routine;
  }

  public PgNotice setRoutine(String routine) {
    this.routine = routine;
    return this;
  }

  public String getSchema() {
    return schema;
  }

  public PgNotice setSchema(String schema) {
    this.schema = schema;
    return this;
  }

  public String getTable() {
    return table;
  }

  public PgNotice setTable(String table) {
    this.table = table;
    return this;
  }

  public String getColumn() {
    return column;
  }

  public PgNotice setColumn(String column) {
    this.column = column;
    return this;
  }

  public String getDataType() {
    return dataType;
  }

  public PgNotice setDataType(String dataType) {
    this.dataType = dataType;
    return this;
  }

  public String getConstraint() {
    return constraint;
  }

  public PgNotice setConstraint(String constraint) {
    this.constraint = constraint;
    return this;
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    PgNoticeConverter.toJson(this, json);
    return json;
  }

  public void log(Logger logger) {
    logger.warn("Backend notice: " +
      "severity='" + getSeverity() + "'" +
      ", code='" + getCode() + "'" +
      ", message='" + getMessage() + "'" +
      ", detail='" + getDetail() + "'" +
      ", hint='" + getHint() + "'" +
      ", position='" + getPosition() + "'" +
      ", internalPosition='" + getInternalPosition() + "'" +
      ", internalQuery='" + getInternalQuery() + "'" +
      ", where='" + getWhere() + "'" +
      ", file='" + getFile() + "'" +
      ", line='" + getLine() + "'" +
      ", routine='" + getRoutine() + "'" +
      ", schema='" + getSchema() + "'" +
      ", table='" + getTable() + "'" +
      ", column='" + getColumn() + "'" +
      ", dataType='" + getDataType() + "'" +
      ", constraint='" + getConstraint() + "'");
  }

  @Override
  public String toString() {
    return "NoticeResponse{" +
      "severity='" + getSeverity() + '\'' +
      ", code='" + getCode() + '\'' +
      ", message='" + getMessage() + '\'' +
      ", detail='" + getDetail() + '\'' +
      ", hint='" + getHint() + '\'' +
      ", position='" + getPosition() + '\'' +
      ", internalPosition='" + getInternalPosition() + '\'' +
      ", internalQuery='" + getInternalQuery() + '\'' +
      ", where='" + getWhere() + '\'' +
      ", file='" + getFile() + '\'' +
      ", line='" + getLine() + '\'' +
      ", routine='" + getRoutine() + '\'' +
      ", schema='" + getSchema() + '\'' +
      ", table='" + getTable() + '\'' +
      ", column='" + getColumn() + '\'' +
      ", dataType='" + getDataType() + '\'' +
      ", constraint='" + getConstraint() + '\'' +
      '}';
  }
}

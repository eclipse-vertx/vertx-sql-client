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

package io.reactiverse.pgclient;

import io.reactiverse.pgclient.impl.codec.decoder.ErrorResponse;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgException extends RuntimeException {

  private final ErrorResponse error;

  public PgException(ErrorResponse error) {
    super(error.getMessage());
    this.error = error;
  }

  public String getSeverity() {
    return error.getSeverity();
  }

  public String getCode() {
    return error.getCode();
  }

  public String getMessage() {
    return error.getMessage();
  }

  public String getDetail() {
    return error.getDetail();
  }

  public String getHint() {
    return error.getHint();
  }

  public String getPosition() {
    return error.getPosition();
  }

  public String getInternalPosition() {
    return error.getInternalPosition();
  }

  public String getInternalQuery() {
    return error.getInternalQuery();
  }

  public String getWhere() {
    return error.getWhere();
  }

  public String getFile() {
    return error.getFile();
  }

  public String getLine() {
    return error.getLine();
  }

  public String getRoutine() {
    return error.getRoutine();
  }

  public String getSchema() {
    return error.getSchema();
  }

  public String getTable() {
    return error.getTable();
  }

  public String getColumn() {
    return error.getColumn();
  }

  public String getDataType() {
    return error.getDataType();
  }

  public String getConstraint() {
    return error.getConstraint();
  }
}

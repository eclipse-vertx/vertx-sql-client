/*
 * Copyright (C) 2019,2020 IBM Corporation
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
 */
package io.vertx.db2client;

/**
 * A {@link RuntimeException} signals that an error occurred.
 */
public class DB2Exception extends RuntimeException {
  private static final long serialVersionUID = 4249056398546361175L;
  
  private final int errorCode;
  private final String sqlState;

  public DB2Exception(String message, int errorCode, String sqlState) {
    super(message);
    this.errorCode = errorCode;
    this.sqlState = sqlState;
  }

  /**
   * Get the error code in the error message sent from DB2 server.
   *
   * @return the error code
   */
  public int getErrorCode() {
    return errorCode;
  }

  /**
   * Get the SQL state in the error message sent from DB2 server.
   *
   * @return the SQL state
   */
  public String getSqlState() {
    return sqlState;
  }

  /**
   * Get the error message in the error message sent from DB2 server.
   *
   * @return the error message
   */
  @Override
  public String getMessage() {
    return super.getMessage();
  }
}

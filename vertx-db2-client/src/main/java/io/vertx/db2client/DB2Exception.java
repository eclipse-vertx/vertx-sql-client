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

import io.vertx.sqlclient.DatabaseException;

/**
 * The {@link DatabaseException} for DB2.
 */
public class DB2Exception extends DatabaseException {
  private static final long serialVersionUID = -1793293963771077543L;

  public DB2Exception(int errorCode, String sqlState) {
    super(formatMessage(null, errorCode, sqlState), errorCode, sqlState);
  }

  public DB2Exception(String message, int errorCode, String sqlState) {
    super(formatMessage(message, errorCode, sqlState), errorCode, sqlState);
  }

  private static String formatMessage(String message, int errorCode, String sqlState) {
    return (message != null ? message : "An error occurred with a DB2 operation") + ", SQLCODE=" + errorCode + "  SQLSTATE=" + sqlState;
  }
}

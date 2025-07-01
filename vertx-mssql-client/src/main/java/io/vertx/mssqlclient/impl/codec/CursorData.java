/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl.codec;

/**
 * Data related to a server cursor.
 */
class CursorData {

  // We need to store the cursor prepared handle here and not use the value inside the prepared statement.
  // On MSSQL, a specific handle is returned when invoking CursorPrepExec.
  int preparedHandle;
  int serverCursorId;
  // When invoking CursorPrepExec, the database server returns column metadata.
  // We store it so that we can tell the server not to return column metadata again when invoking CursorFetch
  MSSQLRowDescriptor mssqlRowDesc;
  boolean fetchSent;
  int rowsTotal;
  int rowsFetched;

  boolean hasMore() {
    return rowsFetched != rowsTotal;
  }

  void setExecutedSqlDirectly() {
    serverCursorId = -1;
  }

  boolean executedSqlDirectly() {
    return serverCursorId == -1;
  }
}

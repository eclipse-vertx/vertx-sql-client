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

package io.vertx.oracleclient.impl;

import io.vertx.sqlclient.internal.RowDescriptor;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class OracleRowDescriptor extends RowDescriptor {

  public static final OracleRowDescriptor EMPTY = new OracleRowDescriptor(new OracleColumnDesc[0]);

  private OracleRowDescriptor(OracleColumnDesc[] columnDescriptors) {
    super(columnDescriptors);
  }

  public static OracleRowDescriptor create(ResultSetMetaData metaData) throws SQLException {
    if (metaData == null) {
      return EMPTY;
    }
    int cols = metaData.getColumnCount();
    OracleColumnDesc[] columnDescriptors = new OracleColumnDesc[cols];
    for (int i = 0; i < cols; i++) {
      columnDescriptors[i] = new OracleColumnDesc(metaData, i + 1);
    }
    return new OracleRowDescriptor(columnDescriptors);
  }
}

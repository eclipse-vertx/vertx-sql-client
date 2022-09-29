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

package io.vertx.db2client.impl.codec;

import io.vertx.sqlclient.desc.ColumnDescriptor;

import java.sql.JDBCType;

class DB2ColumnDesc implements ColumnDescriptor {

  private final String name;
  private final JDBCType type;

  DB2ColumnDesc(String name, JDBCType type) {
    this.name = name;
    this.type = type;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public JDBCType jdbcType() {
    return type;
  }

  @Override
  public boolean isArray() {
    // Array don't seem supported for the moment
    return false;
  }

  @Override
  public String typeName() {
    return null;
  }
}

/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl.codec;

import io.vertx.sqlclient.desc.ColumnDescriptor;

import java.sql.JDBCType;

public final class ColumnData implements ColumnDescriptor {

  private final String name;
  private final DataType dataType;
  private final DataType.Metadata metadata;

  public ColumnData(String name, DataType dataType, DataType.Metadata metadata) {
    this.dataType = dataType;
    this.name = name;
    this.metadata = metadata;
  }

  public DataType dataType() {
    return dataType;
  }

  public DataType.Metadata metadata() {
    return metadata;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public boolean isArray() {
    return false;
  }

  @Override
  public String typeName() {
    return dataType.toString();
  }

  @Override
  public JDBCType jdbcType() {
    return dataType.jdbcType(metadata);
  }
}

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

package io.vertx.sqlclient.impl;

import io.vertx.sqlclient.desc.ColumnDescriptor;

import java.sql.JDBCType;

public class TestRowDesc extends RowDesc {

  private TestRowDesc(TestColumnDescriptor[] columnDescriptors) {
    super(columnDescriptors);
  }

  public static TestRowDesc create(String... names) {
    TestColumnDescriptor[] columnDescriptors = new TestColumnDescriptor[names.length];
    for (int i = 0; i < names.length; i++) {
      columnDescriptors[i] = new TestColumnDescriptor(names[i]);
    }
    return new TestRowDesc(columnDescriptors);
  }

  public static class TestColumnDescriptor implements ColumnDescriptor {

    private final String name;

    public TestColumnDescriptor(String name) {
      this.name = name;
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
      return null;
    }

    @Override
    public JDBCType jdbcType() {
      return JDBCType.OTHER;
    }
  }
}

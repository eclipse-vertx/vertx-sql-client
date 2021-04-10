/*
 *
 *  * Copyright (c) 2021 Vladimir Vishnevsky
 *  *
 *  * This program and the accompanying materials are made available under the
 *  * terms of the Eclipse Public License 2.0 which is available at
 *  * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 *  * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  *
 *  * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package io.vertx.clickhouse.clickhousenative.impl;

import io.vertx.sqlclient.desc.ColumnDescriptor;
import io.vertx.sqlclient.impl.RowDesc;

import java.util.Collections;
import java.util.List;

public class ClickhouseNativeRowDesc extends RowDesc {
  public static final ClickhouseNativeRowDesc EMPTY = new ClickhouseNativeRowDesc(Collections.emptyList(), Collections.emptyList());

  public ClickhouseNativeRowDesc(List<String> columnNames) {
    super(columnNames);
  }

  public ClickhouseNativeRowDesc(List<String> columnNames, List<ColumnDescriptor> columnDescriptors) {
    super(columnNames, columnDescriptors);
  }
}

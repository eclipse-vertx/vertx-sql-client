/*
 * Copyright (c) 2011-2025 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.sqlclient.desc;

import io.vertx.codegen.annotations.VertxGen;

import java.util.List;

/**
 * Describe a database row.
 */
@VertxGen
public interface RowDescriptor {

  /**
   * Get the index of the named column or {@literal -1} when not found
   * @param columnName the column to lookup
   * @return the index of the column
   */
  int columnIndex(String columnName);

  /**
   * @return the list of the column names
   */
  List<String> columnNames();

  /**
   * @return the list of column descriptors
   */
  List<ColumnDescriptor> columnDescriptors();

}

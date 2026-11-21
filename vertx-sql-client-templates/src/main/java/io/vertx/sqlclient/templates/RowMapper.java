/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.sqlclient.templates;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;

/**
 * Map a {@link Row} to an arbitrary {@code T} object.
 */
@VertxGen
@FunctionalInterface
public interface RowMapper<T> {

  /**
   * Create a mapper that converts a {@link Row} to an instance of the given {@code type}.
   *
   * <p>This feature relies on {@link io.vertx.core.json.JsonObject#mapTo} feature. This likely requires
   * to use Jackson databind in the project.
   *
   * @param type the target class
   * @return the mapper
   */
  static <T> RowMapper<T> mapper(Class<T> type) {
    return row -> {
      JsonObject json = new JsonObject();
      for (int i = 0; i < row.size(); i++) {
        json.getMap().put(row.getColumnName(i), row.getValue(i));
      }
      return json.mapTo(type);
    };
  }

  /**
   * Build a {@code T} representation of the given {@code row}
   *
   * @param row the row
   * @return the object
   */
  T map(Row row);

}

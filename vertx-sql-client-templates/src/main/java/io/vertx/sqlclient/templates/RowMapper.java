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
import io.vertx.sqlclient.Row;

/**
 * Map a {@link Row} to an arbitrary {@code T} object.
 */
@VertxGen
@FunctionalInterface
public interface RowMapper<T> {

  /**
   * Build a {@code T} representation of the given {@code row}
   *
   * @param row the row
   * @return the object
   */
  T map(Row row);

}

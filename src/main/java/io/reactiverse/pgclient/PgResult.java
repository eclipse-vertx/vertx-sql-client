/*
 * Copyright (C) 2017 Julien Viet
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
 *
 */

package io.reactiverse.pgclient;

import io.vertx.codegen.annotations.VertxGen;

import java.util.List;

/**
 * Represents the result of an operation on database.
 * @param <T>
 */
@VertxGen
public interface PgResult<T> {

  /**
   * Get the number of the affected rows in the operation to this PgResult.
   * <p/>
   * The meaning depends on the executed statement:
   * <ul>
   *   <li>INSERT: the number of rows inserted</li>
   *   <li>DELETE: the number of rows deleted</li>
   *   <li>UPDATE: the number of rows updated</li>
   *   <li>SELECT: the number of rows retrieved</li>
   * </ul>
   *
   * @return the count of affected rows.
   */
  int rowCount();

  /**
   * Get the names of columns in the PgResult.
   *
   * @return the list of names of columns.
   */
  List<String> columnsNames();

  /**
   * Get the number of rows in the PgResult.
   *
   * @return the count of rows.
   */
  int size();

  /**
   * Get the result value.
   *
   * @return the result
   */
  T value();

  /**
   * Return the next available result or {@code null}, e.g for a simple query that executed multiple queries or for
   * a batch result.
   *
   * @return the next available result or {@code null} if none is available
   */
  PgResult<T> next();

}

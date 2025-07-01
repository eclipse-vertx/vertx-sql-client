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

package io.vertx.sqlclient;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.sqlclient.desc.ColumnDescriptor;
import io.vertx.sqlclient.desc.RowDescriptor;

import java.util.List;

/**
 * An interface that represents the execution result of an operation on the database server.
 *
 * @param <T> the execution result type
 */
@VertxGen
public interface SqlResult<T> {

  /**
   * Get the number of the affected rows in the operation to this SqlResult.
   *
   * @return the count of affected rows.
   */
  int rowCount();

  RowDescriptor rowDescriptor();

  /**
   * Get the names of columns in the SqlResult.
   *
   * @return the list of names of columns.
   */
  List<String> columnsNames();

  /**
   * Get the column descriptors in the SqlResult.
   *
   * @return the list of column descriptors
   */
  List<ColumnDescriptor> columnDescriptors();

  /**
   * Get the number of rows retrieved in the SqlResult.
   *
   * @return the count of rows.
   */
  int size();

  /**
   * Get the specific property with the specified {@link PropertyKind}.
   *
   * @param propertyKind the unique object which is used to indicate which property of the execution result to fetch
   * @param <V> the type of the property value
   * @return the value of the property
   */
  <V> V property(PropertyKind<V> propertyKind);

  /**
   * Get the execution result value, the execution result type may vary such as a {@link RowSet rowSet} or even a {@link String string}.
   *
   * @return the result value
   */
  T value();

  /**
   * Return the next available result or {@code null}, e.g for a simple query that executed multiple queries or for
   * a batch result.
   *
   * @return the next available result or {@code null} if none is available
   */
  SqlResult<T> next();

}

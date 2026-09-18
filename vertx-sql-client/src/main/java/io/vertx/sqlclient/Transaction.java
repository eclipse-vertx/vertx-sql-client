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
import io.vertx.core.Future;

/**
 * A transaction.
 */
@VertxGen
public interface Transaction {

  /**
   * Create a savepoint in this transaction.
   *
   * <p>Fails with {@link UnsupportedOperationException} when the driver does not
   * support savepoints.
   *
   * @return a future notified with the created savepoint
   */
  Future<Savepoint> createSavepoint();

  /**
   * Create a savepoint named {@code name} in this transaction.
   *
   * <p>The name is written to the statement as an unquoted identifier, so it must start
   * with a letter and continue with letters, digits or underscores. Any other name is
   * rejected with an {@link IllegalArgumentException}. Names are scoped to the
   * transaction, creating a savepoint with the name of an existing one replaces it.
   *
   * <p>Fails with {@link UnsupportedOperationException} when the driver does not
   * support savepoints.
   *
   * @param name the savepoint name
   * @return a future notified with the created savepoint
   */
  Future<Savepoint> createSavepoint(String name);

  /**
   * Commit the current transaction.
   */
  Future<Void> commit();

  /**
   * Rollback the transaction and release the associated resources.
   */
  Future<Void> rollback();

  /**
   * Return the transaction completion {@code Future} that
   * succeeds when the transaction commits and
   * fails with {@link TransactionRollbackException} when the transaction rolls back.
   *
   * @return the transaction result
   */
  Future<Void> completion();

}

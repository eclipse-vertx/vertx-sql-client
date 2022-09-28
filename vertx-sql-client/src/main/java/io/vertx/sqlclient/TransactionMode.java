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

package io.vertx.sqlclient;

import java.util.function.Function;

/**
 * Defines how the acquired connection will be managed during the execution of the function provided in
 * {@link Pool#withTransaction(TransactionMode, Function)}.
 */
public enum TransactionMode {

  /**
   * The acquired connection is not stored anywhere, making it local to the provided function execution and to
   * whereever it is passed.
   */
  DEFAULT,

  /**
   * Keeps the acquired connection stored in the local context for as long as the given function executes.
   * Any subsequent calls to {@link Pool#withTransaction} with this mode during the function execution
   * will retrieve this connection from the context instead of creating another.
   * The connection is removed from the local context when the function block has completed.
   */
  PROPAGATABLE

}

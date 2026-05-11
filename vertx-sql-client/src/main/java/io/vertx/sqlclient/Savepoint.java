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
package io.vertx.sqlclient;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;

/**
 * A savepoint created from a {@link Transaction}.
 *
 * <p>A savepoint marks a position inside the current transaction that can later
 * be rolled back to, or released when no longer needed.
 */
@VertxGen
public interface Savepoint {

  /**
   * Roll back the current transaction to this savepoint.
   *
   * <p>The transaction remains active after a successful rollback.
   */
  Future<Void> rollback();

  /**
   * Release this savepoint.
   *
   * <p>After release, this savepoint can no longer be used.
   */
  Future<Void> release();
}

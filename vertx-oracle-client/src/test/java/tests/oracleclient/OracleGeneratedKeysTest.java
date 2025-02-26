/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package tests.oracleclient;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.sqlclient.SqlClient;

import java.util.function.Function;

public class OracleGeneratedKeysTest extends OracleGeneratedKeysTestBase {

  @Override
  protected <T> void withSqlClient(Function<SqlClient, Future<T>> function, Handler<AsyncResult<T>> handler) {
    pool.withConnection(function::apply).onComplete(handler);
  }
}

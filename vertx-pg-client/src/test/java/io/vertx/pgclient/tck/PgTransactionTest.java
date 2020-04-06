/*
 * Copyright (C) 2020 IBM Corporation
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
 */
package io.vertx.pgclient.tck;

import org.junit.ClassRule;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.pgclient.junit.ContainerPgRule;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.tck.TransactionTestBase;

@RunWith(VertxUnitRunner.class)
public class PgTransactionTest extends TransactionTestBase {

  @ClassRule
  public static ContainerPgRule rule = new ContainerPgRule();

  @Override
  protected Pool createPool() {
    return PgPool.pool(vertx, new PgConnectOptions(rule.options()), new PoolOptions().setMaxSize(1));
  }

  @Override
  protected Pool nonTxPool() {
    return PgPool.pool(vertx, new PgConnectOptions(rule.options()), new PoolOptions().setMaxSize(1));
  }

  @Override
  protected String statement(String... parts) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < parts.length; i++) {
      if (i > 0) {
        sb.append("$").append((i));
      }
      sb.append(parts[i]);
    }
    return sb.toString();
  }

}

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
package io.vertx.mssqlclient.tck;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.mssqlclient.MSSQLPool;
import io.vertx.mssqlclient.junit.MSSQLRule;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.tck.TransactionTestBase;

@Ignore // TODO: Implement transaction support
@RunWith(VertxUnitRunner.class)
public class MSSQLTransactionTest extends TransactionTestBase {
  
  @ClassRule
  public static MSSQLRule rule = MSSQLRule.SHARED_INSTANCE;
  
  @Override
  protected void initConnector() {
    connector = handler -> {
      if (pool == null) {
        pool = MSSQLPool.pool(vertx, new MSSQLConnectOptions(rule.options()), new PoolOptions().setMaxSize(1));
      }
      pool.begin(handler);
    };
  }
  
  @Override
  protected Pool nonTxPool() {
    return MSSQLPool.pool(vertx, new MSSQLConnectOptions(rule.options()), new PoolOptions().setMaxSize(1));
  }
  
  @Override
  protected String statement(String... parts) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < parts.length; i++) {
      if (i > 0) {
        sb.append("@p").append((i));
      }
      sb.append(parts[i]);
    }
    return sb.toString();
  }
  
}

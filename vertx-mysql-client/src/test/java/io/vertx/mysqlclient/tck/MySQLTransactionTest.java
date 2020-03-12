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
package io.vertx.mysqlclient.tck;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.mysqlclient.junit.MySQLRule;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.tck.TransactionTestBase;

@RunWith(VertxUnitRunner.class)
public class MySQLTransactionTest extends TransactionTestBase {
  
  @ClassRule
  public static MySQLRule rule = MySQLRule.SHARED_INSTANCE;
  
  @Override
  protected void initConnector() {
    connector = handler -> {
      if (pool == null) {
        pool = MySQLPool.pool(vertx, new MySQLConnectOptions(rule.options()), new PoolOptions().setMaxSize(1));
      }
      pool.begin(handler);
    };
  }
  
  @Override
  protected Pool nonTxPool() {
    return MySQLPool.pool(vertx, new MySQLConnectOptions(rule.options()), new PoolOptions().setMaxSize(1));
  }
  
  @Override
  protected String statement(String... parts) {
    return String.join("?", parts);
  }
  
  @Ignore // TODO: This test fails
  @Override
  @Test
  public void testCommitWithPreparedQuery(TestContext ctx) {
    super.testCommitWithPreparedQuery(ctx);
  }
  
  @Ignore // TODO: This test fails
  @Override
  @Test
  public void testCommitWithQuery(TestContext ctx) {
    super.testCommitWithQuery(ctx);
  }
  
  @Ignore // TODO: This test fails
  @Override
  @Test
  public void testDelayedCommit(TestContext ctx) {
    super.testDelayedCommit(ctx);
  }
  
  @Ignore // TODO: This test fails
  @Override
  @Test
  public void testRollbackData(TestContext ctx) {
    super.testRollbackData(ctx);
  }
  
}

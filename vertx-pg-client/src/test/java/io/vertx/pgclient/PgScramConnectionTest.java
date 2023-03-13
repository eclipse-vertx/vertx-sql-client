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

package io.vertx.pgclient;

import static org.junit.Assume.assumeTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.pgclient.junit.ContainerPgRule;

@RunWith(VertxUnitRunner.class)
public class PgScramConnectionTest {

  @ClassRule
  public static ContainerPgRule rule = new ContainerPgRule();

  private Vertx vertx;

  private PgConnectOptions options;

  @Before
  public void setup() throws Exception {
    vertx = Vertx.vertx();
    options = rule.options();
  }

  private PgConnectOptions options() {
    return new PgConnectOptions(options);
  }

  @Test
  public void testSaslConnection(TestContext ctx) throws InterruptedException {
    assumeTrue(ContainerPgRule.isAtLeastPg10());
    Async async = ctx.async();
    PgConnectOptions options = new PgConnectOptions(options());
    options.setUser("saslscram");
    options.setPassword("saslscrampwd");

    PgConnection.connect(vertx, options).onComplete(
        ctx.asyncAssertSuccess(ar -> {
          ctx.assertNotNull(ar);
          async.complete();
        })
    );
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

}

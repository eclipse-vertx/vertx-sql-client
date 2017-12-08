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
package com.julienviet.pgclient;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class PubSubTest extends PgTestBase {

  Vertx vertx;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testNotify(TestContext ctx) {
    Async async = ctx.async(2);
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.createQuery("LISTEN the_channel").execute(ctx.asyncAssertSuccess(result1 -> {
        conn.notificationHandler(notification -> {
          ctx.assertEquals("the_channel", notification.getChannel());
          ctx.assertEquals("the message", notification.getPayload());
          async.countDown();
        });
        conn.createQuery("NOTIFY the_channel, 'the message'").execute(ctx.asyncAssertSuccess(result2 -> {
          async.countDown();
        }));
      }));
    }));
  }

}

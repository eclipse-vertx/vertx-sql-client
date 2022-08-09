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
package io.vertx.mssqlclient;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(VertxUnitRunner.class)
public class MSSQLInfoTest extends MSSQLTestBase {
  Vertx vertx;
  MSSQLConnection connection;

  @Before
  public void setup(TestContext ctx) {
    vertx = Vertx.vertx();
    options = new MSSQLConnectOptions(MSSQLTestBase.options);
    MSSQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> this.connection = conn));
  }

  @After
  public void tearDown(TestContext ctx) {
    if (connection != null) {
      connection.close(ctx.asyncAssertSuccess());
    }
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testHandleInfo(TestContext ctx) {
    String msg = "Hi there";
    Async async = ctx.async();

    List<MSSQLInfo> infos = Collections.synchronizedList(new ArrayList<>());
    connection.infoHandler(e -> {
      infos.add(e);
      async.complete();
    });
    connection.query(String.format("PRINT '%s'", msg)).execute(ctx.asyncAssertSuccess());

    async.await();

    assertEquals(1, infos.size());
    MSSQLInfo info = infos.get(0);
    assertEquals(msg, info.getMessage());
    assertEquals(0, info.getSeverity());
  }
}

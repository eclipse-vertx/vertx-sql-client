/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.tests.mssqlclient;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.mssqlclient.MSSQLConnection;
import io.vertx.mssqlclient.MSSQLException;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(VertxUnitRunner.class)
public class MSSQLConnectionTest extends MSSQLTestBase {
  Vertx vertx;
  MSSQLConnectOptions options;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = new MSSQLConnectOptions(MSSQLTestBase.options);
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  @Test
  public void testConnectWithEmptyProperties(TestContext ctx) {
    options.setProperties(new HashMap<>());
    MSSQLConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(SqlConnection::close));
  }

  @Test
  public void testInflightCommandsFailWhenConnectionClosed(TestContext ctx) {
    MSSQLConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn1 -> {
      conn1
        .query("WAITFOR DELAY '00:00:20'")
        .execute()
        .onComplete(ctx.asyncAssertFailure(t -> {
        ctx.verify(v -> {
          assertThat(t, instanceOf(MSSQLException.class));
          assertEquals("Cannot continue the execution because the session is in the kill state.", ((MSSQLException) t).errorMessage());
        });
      }));
      MSSQLConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn2 -> {
        conn2
          .preparedQuery("EXEC sp_who;")
          .execute()
          .onComplete(ctx.asyncAssertSuccess(processRes -> {
          for (Row row : processRes) {
            Short spid = row.getShort("spid");
            String cmd = row.getString("cmd");
            if (cmd.contains("WAITFOR")) {
              conn2
                .query(String.format("KILL %d", spid))
                .execute()
                .onComplete(ctx.asyncAssertSuccess(v -> conn2.close()));
              break;
            }
          }
        }));
      }));
    }));
  }
}

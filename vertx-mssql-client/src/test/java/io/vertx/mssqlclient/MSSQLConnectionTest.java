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

package io.vertx.mssqlclient;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

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
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testConnectWithEmptyProperties(TestContext ctx) {
    options.setProperties(new HashMap<>());
    MSSQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(SqlConnection::close));
  }

  @Test
  @Ignore("unsupported command")
  // TODO stored procedure response is handled incorrectly, enable the test after https://github.com/eclipse-vertx/vertx-sql-client/issues/856 is solved, just don't break the CI and snapshot release for now
  public void testInflightCommandsFailWhenConnectionClosed(TestContext ctx) {
    MSSQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn1 -> {
      conn1.query("WAITFOR DELAY '00:00:20'").execute(ctx.asyncAssertFailure(t -> {
        ctx.assertEquals("Cannot continue the execution because the session is in the kill state.", t.getMessage());
      }));
      MSSQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn2 -> {
        conn2.preparedQuery("EXEC sp_who;").execute(ctx.asyncAssertSuccess(processRes -> {
          for (Row row : processRes) {
            Short spid = row.getShort("spid");
            String cmd = row.getString("cmd");
            if (cmd.contains("WAITFOR")) {
              conn2.query(String.format("KILL %d", spid)).execute(ctx.asyncAssertSuccess(v -> conn2.close()));
              break;
            }
          }
        }));
      }));
    }));
  }
}

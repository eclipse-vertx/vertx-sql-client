/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mysqlclient;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MySQLUtilityCommandTest extends MySQLTestBase {

  Vertx vertx;
  MySQLConnectOptions options;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = new MySQLConnectOptions(MySQLTestBase.options);
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  @Test
  public void testPingCommand(TestContext ctx) {
    MySQLConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .ping()
        .onComplete(ctx.asyncAssertSuccess(v -> {
        conn.close();
      }));
    }));
  }

  @Test
  public void testChangeSchema(TestContext ctx) {
    MySQLConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT DATABASE();")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals("testschema", result.iterator().next().getString(0));
        conn
          .specifySchema("emptyschema")
          .onComplete(ctx.asyncAssertSuccess(v -> {
          conn
            .query("SELECT DATABASE();")
            .execute()
            .onComplete(ctx.asyncAssertSuccess(result2 -> {
            ctx.assertEquals("emptyschema", result2.iterator().next().getString(0));
            conn.close();
          }));
        }));
      }));
    }));
  }

  @Test
  public void testChangeToInvalidSchema(TestContext ctx) {
    MySQLConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT DATABASE();")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals("testschema", result.iterator().next().getString(0));
        conn
          .specifySchema("invalidschema")
          .onComplete(ctx.asyncAssertFailure(error -> {
          conn.close();
        }));
      }));
    }));
  }

  @Test
  public void testStatistics(TestContext ctx) {
    MySQLConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .getInternalStatistics()
        .onComplete(ctx.asyncAssertSuccess(result -> {
        ctx.assertTrue(!result.isEmpty());
        conn.close();
      }));
    }));
  }

  @Test
  public void testSetOption(TestContext ctx) {
    MySQLConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      // CLIENT_MULTI_STATEMENTS is on by default
      conn.query("SELECT 1; SELECT 2;")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(rowSet1 -> {
        ctx.assertEquals(1, rowSet1.size());
        Row row1 = rowSet1.iterator().next();
        ctx.assertEquals(1, row1.getInteger(0));
        RowSet<Row> rowSet2 = rowSet1.next();
        ctx.assertEquals(1, rowSet2.size());
        Row row2 = rowSet2.iterator().next();
        ctx.assertEquals(2, row2.getInteger(0));

        conn
          .setOption(MySQLSetOption.MYSQL_OPTION_MULTI_STATEMENTS_OFF)
          .onComplete(ctx.asyncAssertSuccess(v -> {
          // CLIENT_MULTI_STATEMENTS is off now
          conn
            .query("SELECT 1; SELECT 2;")
            .execute()
            .onComplete(ctx.asyncAssertFailure(error -> {
            conn.close();
          }));
        }));
      }));
    }));
  }

  @Test
  public void testResetConnection(TestContext ctx) {
    Assume.assumeFalse(rule.isUsingMySQL5_6());
    MySQLConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("CREATE TEMPORARY TABLE temp (temp INTEGER)")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(res1 -> {
        conn
          .query("SELECT * FROM temp")
          .execute()
          .onComplete(ctx.asyncAssertSuccess(res2 -> {
          conn
            .resetConnection()
            .onComplete(ctx.asyncAssertSuccess(res3 -> {
            conn
              .query("SELECT * FROM temp")
              .execute()
              .onComplete(ctx.asyncAssertFailure(error -> {
              conn.close();
            }));
          }));
        }));
      }));
    }));
  }

  @Test
  public void testChangeUser(TestContext ctx) {
    MySQLConnection.connect(vertx, options).onComplete( ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT current_user()")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(res1 -> {
        Row row1 = res1.iterator().next();
        String username = row1.getString(0);
        ctx.assertEquals("mysql", username.substring(0, username.lastIndexOf('@')));
        MySQLAuthOptions changeUserOptions = new MySQLAuthOptions()
          .setUser("superuser")
          .setPassword("password")
          .setDatabase("emptyschema");
        conn
          .changeUser(changeUserOptions)
          .onComplete(ctx.asyncAssertSuccess(v2 -> {
          conn
            .query("SELECT current_user();SELECT database();")
            .execute()
            .onComplete(ctx.asyncAssertSuccess(res2 -> {
            ctx.assertEquals("superuser@%", res2.iterator().next().getString(0));
            ctx.assertEquals("emptyschema", res2.next().iterator().next().getValue(0));
            conn.close();
          }));
        }));
      }));
    }));
  }

  @Test
  public void testChangeUserAuthWithServerRsaPublicKey(TestContext ctx) {
    MySQLConnection.connect(vertx, options).onComplete( ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT current_user()")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(res1 -> {
        Row row1 = res1.iterator().next();
        String username = row1.getString(0);
        ctx.assertEquals("mysql", username.substring(0, username.lastIndexOf('@')));
        MySQLAuthOptions changeUserOptions = new MySQLAuthOptions()
          .setUser("superuser")
          .setPassword("password")
          .setDatabase("emptyschema")
          .setServerRsaPublicKeyValue(Buffer.buffer("-----BEGIN PUBLIC KEY-----\n" +
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3yvG5s0qrV7jxVlp0sMj\n" +
            "xP0a6BuLKCMjb0o88hDsJ3xz7PpHNKazuEAfPxiRFVAV3edqfSiXoQw+lJf4haEG\n" +
            "HQe12Nfhs+UhcAeTKXRlZP/JNmI+BGoBduQ1rCId9bKYbXn4pvyS/a1ft7SwFkhx\n" +
            "aogCur7iIB0WUWvwkQ0fEj/Mlhw93lLVyx7hcGFq4FOAKFYr3A0xrHP1IdgnD8QZ\n" +
            "0fUbgGLWWLOossKrbUP5HWko1ghLPIbfmU6o890oj1ZWQewj1Rs9Er92/UDj/JXx\n" +
            "7ha1P+ZOgPBlV037KDQMS6cUh9vTablEHsMLhDZanymXzzjBkL+wH/b9cdL16LkQ\n" +
            "5QIDAQAB\n" +
            "-----END PUBLIC KEY-----\n"));
        conn
          .changeUser(changeUserOptions)
          .onComplete(ctx.asyncAssertSuccess(v2 -> {
          conn
            .query("SELECT current_user();SELECT database();")
            .execute()
            .onComplete(ctx.asyncAssertSuccess(res2 -> {
            ctx.assertEquals("superuser@%", res2.iterator().next().getString(0));
            ctx.assertEquals("emptyschema", res2.next().iterator().next().getValue(0));
            conn.close();
          }));
        }));
      }));
    }));
  }
}

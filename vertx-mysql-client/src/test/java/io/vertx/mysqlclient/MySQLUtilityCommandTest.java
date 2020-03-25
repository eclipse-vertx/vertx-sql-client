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
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testPingCommand(TestContext ctx) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.ping(ctx.asyncAssertSuccess(v -> {
        conn.close();
      }));
    }));
  }

  @Test
  public void testChangeSchema(TestContext ctx) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT DATABASE();").execute(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals("testschema", result.iterator().next().getString(0));
        conn.specifySchema("emptyschema", ctx.asyncAssertSuccess(v -> {
          conn.query("SELECT DATABASE();").execute(ctx.asyncAssertSuccess(result2 -> {
            ctx.assertEquals("emptyschema", result2.iterator().next().getString(0));
            conn.close();
          }));
        }));
      }));
    }));
  }

  @Test
  public void testChangeToInvalidSchema(TestContext ctx) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT DATABASE();").execute(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals("testschema", result.iterator().next().getString(0));
        conn.specifySchema("invalidschema", ctx.asyncAssertFailure(error -> {
          conn.close();
        }));
      }));
    }));
  }

  @Test
  public void testStatistics(TestContext ctx) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.getInternalStatistics(ctx.asyncAssertSuccess(result -> {
        ctx.assertTrue(!result.isEmpty());
        conn.close();
      }));
    }));
  }

  @Test
  public void testSetOption(TestContext ctx) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      // CLIENT_MULTI_STATEMENTS is on by default
      conn.query("SELECT 1; SELECT 2;").execute(ctx.asyncAssertSuccess(rowSet1 -> {
        ctx.assertEquals(1, rowSet1.size());
        Row row1 = rowSet1.iterator().next();
        ctx.assertEquals(1, row1.getInteger(0));
        RowSet<Row> rowSet2 = rowSet1.next();
        ctx.assertEquals(1, rowSet2.size());
        Row row2 = rowSet2.iterator().next();
        ctx.assertEquals(2, row2.getInteger(0));

        conn.setOption(MySQLSetOption.MYSQL_OPTION_MULTI_STATEMENTS_OFF, ctx.asyncAssertSuccess(v -> {
          // CLIENT_MULTI_STATEMENTS is off now
          conn.query("SELECT 1; SELECT 2;").execute(ctx.asyncAssertFailure(error -> {
            conn.close();
          }));
        }));
      }));
    }));
  }

  @Test
  public void testResetConnection(TestContext ctx) {
    Assume.assumeFalse(rule.isUsingMySQL5_6());
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("CREATE TEMPORARY TABLE temp (temp INTEGER)").execute(ctx.asyncAssertSuccess(res1 -> {
        conn.query("SELECT * FROM temp").execute(ctx.asyncAssertSuccess(res2 -> {
          conn.resetConnection(ctx.asyncAssertSuccess(res3 -> {
            conn.query("SELECT * FROM temp").execute(ctx.asyncAssertFailure(error -> {
              conn.close();
            }));
          }));
        }));
      }));
    }));
  }

  @Test
  public void testChangeUser(TestContext ctx) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT current_user()").execute(ctx.asyncAssertSuccess(res1 -> {
        Row row1 = res1.iterator().next();
        String username = row1.getString(0);
        ctx.assertEquals("mysql", username.substring(0, username.lastIndexOf('@')));
        MySQLAuthOptions changeUserOptions = new MySQLAuthOptions()
          .setUser("superuser")
          .setPassword("password")
          .setDatabase("emptyschema");
        conn.changeUser(changeUserOptions, ctx.asyncAssertSuccess(v2 -> {
          conn.query("SELECT current_user();SELECT database();").execute(ctx.asyncAssertSuccess(res2 -> {
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
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT current_user()").execute(ctx.asyncAssertSuccess(res1 -> {
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
        conn.changeUser(changeUserOptions, ctx.asyncAssertSuccess(v2 -> {
          conn.query("SELECT current_user();SELECT database();").execute(ctx.asyncAssertSuccess(res2 -> {
            ctx.assertEquals("superuser@%", res2.iterator().next().getString(0));
            ctx.assertEquals("emptyschema", res2.next().iterator().next().getValue(0));
            conn.close();
          }));
        }));
      }));
    }));
  }
}

package io.vertx.mysqlclient;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MySQLClientCapabilitiesTest extends MySQLTestBase {

  private static final String PREPARE_TESTING_TABLE_DATA = "CREATE TEMPORARY TABLE vehicle (\n" +
    "\tid INTEGER,\n" +
    "\ttype VARCHAR(20));\n" +
    "INSERT INTO vehicle VALUES (1, 'bike');\n";

  Vertx vertx;
  MySQLConnectOptions options;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = new MySQLConnectOptions(MySQLTestBase.options);
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }


  @Test
  public void testAffectedRowsEnabledClientCapability(TestContext ctx) {
    testAffectedRowsClientCapability(ctx, true, 0);
  }

  @Test
  public void testAffectedRowsDisabledClientCapability(TestContext ctx) {
    testAffectedRowsClientCapability(ctx, false, 1);
  }

  private void testAffectedRowsClientCapability(TestContext ctx, boolean useAffectedRows, int expectedRowCount) {
    MySQLConnectOptions connectOptions = options.setUseAffectedRows(useAffectedRows);
    MySQLConnection.connect(vertx, connectOptions, ctx.asyncAssertSuccess(conn -> {
      conn.query(PREPARE_TESTING_TABLE_DATA, ctx.asyncAssertSuccess(res0 -> {
        conn
          .query("UPDATE vehicle SET type = 'bike' WHERE id = 1;", ctx.asyncAssertSuccess(res1 -> {
            ctx.assertEquals(expectedRowCount, res1.rowCount());
          }));
      }));
    }));
  }
}

package io.vertx.db2client.tck;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.db2client.junit.DB2Resource;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.tck.PreparedQueryCachedTestBase;

@RunWith(VertxUnitRunner.class)
public class DB2PreparedQueryCachedTest extends PreparedQueryCachedTestBase {
  @ClassRule
  public static DB2Resource rule = DB2Resource.SHARED_INSTANCE;

  @Override
  protected void initConnector() {
    options = rule.options();
    connector = ClientConfig.CONNECT.connect(vertx, options);
  }

  @Override
  protected String statement(String... parts) {
    return String.join("?", parts);
  }

  @Override
  protected boolean cursorRequiresTx() {
    return false;
  }

  @Override
  protected void cleanTestTable(TestContext ctx) {
      // use DELETE FROM because DB2 does not support TRUNCATE TABLE
      connect(ctx.asyncAssertSuccess(conn -> {
          conn.query("DELETE FROM mutable").execute(ctx.asyncAssertSuccess(result -> {
              conn.close();
          }));
      }));
  }

  @Test
  @Ignore // TODO: Enable this test after implementing error path handling
  @Override
  public void testPrepareError(TestContext ctx) {
  }

  @Test
  @Ignore // TODO: Enable this test after implementing error path handling
  @Override
  public void testPreparedQueryParamCoercionTypeError(TestContext ctx) {
  }

  @Test
  @Ignore // TODO: Enable this test after implementing error path handling
  @Override
  public void testPreparedQueryParamCoercionQuantityError(TestContext ctx) {
  }

  @Test
  @Ignore // TODO: Enable this test after implementing error path handling
  @Override
  public void testPreparedUpdateWithNullParams(TestContext ctx) {
  }

}

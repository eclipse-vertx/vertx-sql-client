package io.vertx.db2client.tck;

import io.vertx.db2client.junit.DB2Resource;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.tck.PreparedBatchTestBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class DB2PreparedBatchTest extends PreparedBatchTestBase {
  @ClassRule
  public static DB2Resource rule = DB2Resource.SHARED_INSTANCE;

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }
  
  @Override
  protected void cleanTestTable(TestContext ctx) {
      // use DELETE FROM because DB2 does not support TRUNCATE TABLE
      connect(ctx.asyncAssertSuccess(conn -> {
          conn.query("DELETE FROM mutable", ctx.asyncAssertSuccess(result -> {
              conn.close();
          }));
      }));
  }

  @Override
  protected String statement(String... parts) {
    return String.join("?", parts);
  }
  
}

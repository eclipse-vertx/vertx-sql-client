package io.vertx.db2client.tck;

import io.vertx.db2client.junit.DB2Resource;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.tests.sqlclient.tck.PreparedBatchTestBase;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class DB2PreparedBatchTest extends PreparedBatchTestBase {
  @ClassRule
  public static DB2Resource rule = DB2Resource.SHARED_INSTANCE;

  @Rule
  public TestName testName = new TestName();

  @Before
  public void printTestName(TestContext ctx) throws Exception {
    System.out.println(">>> BEGIN " + getClass().getSimpleName() + "." + testName.getMethodName());
  }

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }

  @Override
  protected void cleanTestTable(TestContext ctx) {
      // use DELETE FROM because DB2 does not support TRUNCATE TABLE
      connect(ctx.asyncAssertSuccess(conn -> {
          conn
            .query("DELETE FROM mutable")
            .execute()
            .onComplete(ctx.asyncAssertSuccess(result -> {
              conn.close();
          }));
      }));
  }

  @Override
  protected String statement(String... parts) {
    return String.join("?", parts);
  }

}

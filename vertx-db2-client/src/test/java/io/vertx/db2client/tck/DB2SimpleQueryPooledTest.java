package io.vertx.db2client.tck;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import io.vertx.db2client.junit.DB2Resource;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.tck.SimpleQueryTestBase;

@RunWith(VertxUnitRunner.class)
public class DB2SimpleQueryPooledTest extends SimpleQueryTestBase {
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
        connector = ClientConfig.POOLED.connect(vertx, rule.options());
    }

    @Override
    protected void cleanTestTable(TestContext ctx) {
        connect(ctx.asyncAssertSuccess(conn -> {
            conn.query("DELETE FROM mutable").execute(ctx.asyncAssertSuccess(result -> {
                conn.close();
            }));
        }));
    }
}

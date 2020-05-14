package io.vertx.db2client.tck;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import io.vertx.db2client.junit.DB2Resource;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.tck.CollectorTestBase;

@RunWith(VertxUnitRunner.class)
public class DB2CollectorTest extends CollectorTestBase {
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
}

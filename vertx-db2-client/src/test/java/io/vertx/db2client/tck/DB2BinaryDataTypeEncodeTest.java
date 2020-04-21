package io.vertx.db2client.tck;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import io.vertx.db2client.junit.DB2Resource;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.tck.BinaryDataTypeEncodeTestBase;

@RunWith(VertxUnitRunner.class)
public class DB2BinaryDataTypeEncodeTest extends BinaryDataTypeEncodeTestBase {
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
  protected String statement(String... parts) {
    return String.join("?", parts);
  }

  @Test
  @Override
  public void testDouble(TestContext ctx) {
    // The smallest positive value supported by the DOUBLE column type in DB2 is 5.4E-079
    testEncodeGeneric(ctx, "test_float_8", Double.class, Double.valueOf("5.4E-079"));
  }
  
  @Override
  public void testBoolean(TestContext ctx) {
    if (!rule.isZOS()) {
      super.testBoolean(ctx);
      return;
    }
    
    // DB2/Z doesn't have a BOOLEAN column type and uses TINYINT instead
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery("UPDATE basicdatatype SET test_boolean = ? WHERE id = 2")
        .execute(Tuple.tuple().addValue(false), ctx.asyncAssertSuccess(updateResult -> {
        conn
          .preparedQuery("SELECT test_boolean FROM basicdatatype WHERE id = 2")
          .execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ctx.assertEquals((short) 0, row.getValue(0));
          ctx.assertEquals((short) 0, row.getValue("test_boolean"));
          ctx.assertEquals(false, row.getBoolean(0));
          conn.close();
        }));
      }));
    }));
  }

}

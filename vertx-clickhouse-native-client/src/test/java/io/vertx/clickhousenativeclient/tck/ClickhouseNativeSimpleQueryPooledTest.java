package io.vertx.clickhousenativeclient.tck;

import io.vertx.clickhousenativeclient.ClickhouseResource;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.tck.SimpleQueryTestBase;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ClickhouseNativeSimpleQueryPooledTest extends SimpleQueryTestBase {

  @ClassRule
  public static ClickhouseResource rule = new ClickhouseResource();

  @Override
  protected void initConnector() {
    connector = ClientConfig.POOLED.connect(vertx, rule.options());
  }

  @Ignore
  @Test
  public void testDelete(TestContext ctx) {
    //no way to return count of altered rows
  }

  @Ignore
  @Test
  public void testInsert(TestContext ctx) {
    //no way to return count of inserted rows (can be emulated)
  }

  @Ignore
  @Test
  public void testUpdate(TestContext ctx) {
    //no way to return count of altered rows
  }

  @Override
  protected void cleanTestTable(TestContext ctx) {
    super.cleanTestTable(ctx);
    Sleep.sleepOrThrow();
  }
}

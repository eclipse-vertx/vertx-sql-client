package io.vertx.db2client.tck;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.db2client.junit.DB2Resource;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.tck.CollectorTestBase;

@RunWith(VertxUnitRunner.class)
public class DB2CollectorTest extends CollectorTestBase {
  @ClassRule
  public static DB2Resource rule = DB2Resource.SHARED_INSTANCE;

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }

  @Override
  @Ignore // TODO: implement error paths properly
  @Test
  public void testCollectorFailureProvidingSupplier(TestContext ctx) {
  }

  @Override
  @Ignore // TODO: implement error paths properly
  @Test
  public void testCollectorFailureInSupplier(TestContext ctx) {
  }

  @Override
  @Ignore // TODO: implement error paths properly
  @Test
  public void testCollectorFailureProvidingAccumulator(TestContext ctx) {
  }

  @Override
  @Ignore // TODO: implement error paths properly
  @Test
  public void testCollectorFailureInAccumulator(TestContext ctx) {
  }

  @Override
  @Ignore // TODO: implement error paths properly
  @Test
  public void testCollectorFailureProvidingFinisher(TestContext ctx) {
  }

  @Override
  @Ignore // TODO: implement error paths properly
  @Test
  public void testCollectorFailureInFinisher(TestContext ctx) {
  }
}

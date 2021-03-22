package io.vertx.clickhousenativeclient.tck;

import io.vertx.clickhouse.clickhousenative.ClickhouseNativePool;
import io.vertx.clickhousenativeclient.ClickhouseResource;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.tck.TracingTestBase;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ClickhouseNativeTracingTest extends TracingTestBase {
  @ClassRule
  public static ClickhouseResource rule = new ClickhouseResource();

  @Override
  protected Pool createPool(Vertx vertx) {
    return ClickhouseNativePool.pool(vertx, rule.options(), new PoolOptions());
  }

  @Override
  protected String statement(String... parts) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < parts.length; i++) {
      if (i > 0) {
        sb.append("$").append((i));
      }
      sb.append(parts[i]);
    }
    return sb.toString();
  }

  @Ignore
  @Test
  public void testTraceBatchQuery(TestContext ctx) {
    //no batch support for SELECTs
  }
}

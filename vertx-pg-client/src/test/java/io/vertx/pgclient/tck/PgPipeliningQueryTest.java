package io.vertx.pgclient.tck;

import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.junit.ContainerPgRule;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.tck.PipeliningQueryTestBase;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class PgPipeliningQueryTest extends PipeliningQueryTestBase {
  @ClassRule
  public static ContainerPgRule rule = new ContainerPgRule();

  @Override
  protected void init() {
    options = rule.options();
    PgConnectOptions pgConnectOptions = (PgConnectOptions) options;
    pgConnectOptions.setPipeliningLimit(64);
    connectionConnector = ClientConfig.CONNECT.connect(vertx, options);
    pooledConnectionConnector = ClientConfig.POOLED.connect(vertx, options);
    pooledClientSupplier = () -> PgBuilder.client(b -> b.connectingTo(options).with(new PoolOptions().setMaxSize(8)).using(vertx));
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

  @Override
  protected String buildCounterPreparedQueryWithoutTable() {
    // default type is char
    return statement("SELECT CAST(", " AS INTEGER)");
  }
}

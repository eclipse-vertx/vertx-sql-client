package io.vertx.oracleclient.test.tck;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.oracleclient.OracleBuilder;
import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.oracleclient.test.junit.OracleRule;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.tck.PipeliningQueryTestBase;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(VertxUnitRunner.class)
public class OraclePipeliningQueryTest extends PipeliningQueryTestBase {

  @ClassRule
  public static final OracleRule rule = OracleRule.SHARED_INSTANCE;

  @Override
  protected void cleanTestTable(TestContext ctx) {
    connectionConnector.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("TRUNCATE TABLE mutable")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
          conn.close();
        }));
    }));
  }

  @Override
  protected void init() {
    options = rule.options();
    OracleConnectOptions oracleConnectOptions = (OracleConnectOptions) options;
    oracleConnectOptions.setPipeliningLimit(64);
    connectionConnector = ClientConfig.CONNECT.connect(vertx, oracleConnectOptions);
    pooledConnectionConnector = ClientConfig.POOLED.connect(vertx, oracleConnectOptions);
    pooledClientSupplier = () -> OracleBuilder.pool(b -> b.connectingTo(oracleConnectOptions).with(new PoolOptions().setMaxSize(8)).using(vertx));
  }

  @Override
  protected String statement(String... parts) {
    return String.join("?", parts);
  }

  @Override
  protected void testOneShotPreparedBatchInsert(TestContext ctx, SqlClient client) {
    List<Tuple> batchParams = new ArrayList<>();
    for (int i = 0; i < 1000; i++) {
      batchParams.add(Tuple.of(i, String.format("val-%d", i)));
    }

    client.preparedQuery(statement("INSERT INTO mutable(id, val) VALUES (", ", ",")"))
      .executeBatch(batchParams)
      .onComplete(ctx.asyncAssertSuccess(res -> {
        ctx.assertEquals(1000, res.rowCount());

        client.query("SELECT id, val FROM mutable")
          .execute()
          .onComplete(ctx.asyncAssertSuccess(res2 -> {
            ctx.assertEquals(1000, res2.size());
            int i = 0;
            for (Row row : res2) {
              ctx.assertEquals(2, row.size());
              ctx.assertEquals(i, row.getInteger(0));
              ctx.assertEquals(String.format("val-%d", i), row.getString(1));
              i++;
            }
            client.close();
          }));
      }));
  }

  @Override
  @Ignore("Oracle does not support batching queries")
  public void testOneShotPreparedBatchQueryConn(TestContext ctx) {

  }

  @Override
  @Ignore("Oracle does not support batching queries")
  public void testOneShotPreparedBatchQueryPool(TestContext ctx) {

  }
}

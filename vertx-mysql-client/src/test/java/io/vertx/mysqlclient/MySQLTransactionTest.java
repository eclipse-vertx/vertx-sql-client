package io.vertx.mysqlclient;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.transaction.TransactionAccessMode;
import io.vertx.sqlclient.transaction.TransactionIsolationLevel;
import io.vertx.sqlclient.transaction.TransactionOptions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MySQLTransactionTest extends MySQLTestBase {
  Vertx vertx;
  MySQLConnectOptions options;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = new MySQLConnectOptions(MySQLTestBase.options);
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testTxOptions(TestContext ctx) {
    Async async = ctx.async();
    TransactionOptions txOptions = new TransactionOptions();
    txOptions.setAccessMode(TransactionAccessMode.READ_ONLY);
    txOptions.setIsolationLevel(TransactionIsolationLevel.SERIALIZABLE);
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.begin(txOptions, ctx.asyncAssertSuccess(tx -> {
        conn.query("INSERT INTO mutable (id, val) VALUES (1, 'hello-1')")
          .execute(ctx.asyncAssertFailure(error -> {
            tx.rollback();
            conn.close();
            async.complete();
          }));
      }));
    }));
  }
}

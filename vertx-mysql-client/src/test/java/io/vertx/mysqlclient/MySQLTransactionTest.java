package io.vertx.mysqlclient;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.TransactionAccessMode;
import io.vertx.sqlclient.TransactionIsolationLevel;
import io.vertx.sqlclient.TransactionOptions;
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
    txOptions.setTransactionAccessMode(TransactionAccessMode.READ_ONLY);
    txOptions.setTransactionIsolationLevel(TransactionIsolationLevel.REPEATABLE_READ);
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.begin(txOptions, ctx.asyncAssertSuccess(tx -> {
        conn.query("SELECT @@TX_ISOLATION")
          .execute(ctx.asyncAssertSuccess(res -> {
            ctx.assertEquals("REPEATABLE-READ", res.iterator().next().getString("@@TX_ISOLATION"));
            conn.query("INSERT INTO mutable (id, val) VALUES (1, 'hello-1')")
              .execute(ctx.asyncAssertFailure(error -> {
                tx.rollback();
                conn.close();
                async.complete();
              }));
          }));
      }));
    }));
  }
}

package io.vertx.clickhousenativeclient.tck;

import io.vertx.clickhousenativeclient.ClickhouseResource;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.tck.DataTypeTestBase;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ClickhouseNativeTransactionTest extends DataTypeTestBase {
  @ClassRule
  public static ClickhouseResource rule = new ClickhouseResource();

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }


  @Test
  public void testTransactionsAreNotSupported(TestContext ctx) {
    //transactions are not supported by DB at the moment
    connector.connect(ctx.asyncAssertSuccess(res1 -> {
      res1.begin(ctx.asyncAssertFailure(
        err -> {
          ctx.assertEquals(UnsupportedOperationException.class, err.getClass());
        }
      ));
    }));
  }
}

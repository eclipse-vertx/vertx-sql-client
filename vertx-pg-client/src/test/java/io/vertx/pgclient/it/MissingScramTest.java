package io.vertx.pgclient.it;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgConnection;
import io.vertx.pgclient.junit.ContainerPgRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assume.assumeTrue;

@RunWith(VertxUnitRunner.class)
public class MissingScramTest {

  @ClassRule
  public static ContainerPgRule rule = new ContainerPgRule();

  private Vertx vertx;

  private PgConnectOptions options;

  @Before
  public void setup() throws Exception {
    vertx = Vertx.vertx();
    options = rule.options();
  }

  private PgConnectOptions options() {
    return new PgConnectOptions(options);
  }

  @Test
  public void testSaslConnectionFails(TestContext ctx) throws InterruptedException {
    assumeTrue(ContainerPgRule.isAtLeastPg10());
    Async async = ctx.async();
    PgConnectOptions options = new PgConnectOptions(options());
    options.setUser("saslscram");
    options.setPassword("saslscrampwd");

    PgConnection.connect(vertx, options).onComplete(
      ctx.asyncAssertFailure(ar -> {
        async.complete();
      })
    );
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }
}

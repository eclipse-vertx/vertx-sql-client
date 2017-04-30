package io.vertx.pgclient;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import ru.yandex.qatools.embed.postgresql.PostgresExecutable;
import ru.yandex.qatools.embed.postgresql.PostgresProcess;
import ru.yandex.qatools.embed.postgresql.PostgresStarter;
import ru.yandex.qatools.embed.postgresql.config.AbstractPostgresConfig;
import ru.yandex.qatools.embed.postgresql.config.PostgresConfig;

import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.currentTimeMillis;
import static ru.yandex.qatools.embed.postgresql.distribution.Version.V9_5_0;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@RunWith(VertxUnitRunner.class)
public class PgTest {

  private static PostgresProcess process;
  private Vertx vertx;

  @BeforeClass
  public static void startPg() throws Exception {
    PostgresStarter<PostgresExecutable, PostgresProcess> runtime = PostgresStarter.getDefaultInstance();
    PostgresConfig config = new PostgresConfig(V9_5_0, new AbstractPostgresConfig.Net(),
      new AbstractPostgresConfig.Storage("postgres"), new AbstractPostgresConfig.Timeout(),
      new AbstractPostgresConfig.Credentials("postgres", "postgres"));
    PostgresExecutable exec = runtime.prepare(config);
    process = exec.start();
  }

  @Before
  public void setup() {
    vertx = Vertx.vertx();
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testQuery(TestContext ctx) {
    Async async = ctx.async();
    PgClientOptions options = new PgClientOptions();
    options.setHost(process.getConfig().net().host());
    options.setPort(process.getConfig().net().port());
    options.setDatabase("postgres");
    options.setUsername("postgres");
    options.setPassword("postgres");
    options.setPoolsize(1);
    PgClient client = PgClient.create(vertx, options);
    int amount = 10;
    double sleep = 0.5;
    AtomicInteger completed = new AtomicInteger();
    for (int i = 0;i < amount;i++) {
      int val = i;
      client.query("select " + i + ", pg_sleep(" + sleep + ")", ctx.asyncAssertSuccess(res -> {
        System.out.println("got result " + val + " " + System.currentTimeMillis());
        if (completed.incrementAndGet() == amount) {
          System.out.println("done");
          async.complete();
        }
      }));
    }
    System.out.println("sent " + amount + " queries " + System.currentTimeMillis());
  }
}

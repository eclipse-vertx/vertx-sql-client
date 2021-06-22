package io.vertx.pgclient;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.pgclient.junit.ContainerPgRule;
import io.vertx.sqlclient.PoolConfig;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(VertxUnitRunner.class)
public class PoolMultiTest {

  @ClassRule
  public static final ContainerPgRule db1 = new ContainerPgRule().user("user1");

  @ClassRule
  public static final ContainerPgRule db2 = new ContainerPgRule().user("user2");

  private Vertx vertx;

  @Before
  public void setup() throws Exception {
    vertx = Vertx.vertx();
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testLoadBalancing(TestContext ctx) {
    int count = 5;
    Async async = ctx.async(count);
    PoolConfig config = PoolConfig.create(new PoolOptions().setMaxSize(count))
      .connectingTo(Arrays.asList(db1.options(), db2.options()));
    PgPool pool = PgPool.pool(config);
    List<Future<SqlConnection>> futures = new ArrayList<>();
    for (int i = 0; i < count;i++) {
      futures.add(pool.getConnection());
    }
    List<String> users = Collections.synchronizedList(new ArrayList<>());
    CompositeFuture.all((List)futures).onComplete(ctx.asyncAssertSuccess(c -> {
      for (int i = 0;i < count;i++) {
        SqlConnection conn = futures.get(i).result();
        conn.query("SELECT user").execute(ctx.asyncAssertSuccess(res -> {
          users.add(res.iterator().next().getString(0));
          conn.close(ctx.asyncAssertSuccess(v -> {
            async.countDown();
          }));
        }));
      }
    }));
    async.awaitSuccess(20_000);
    assertEquals(5, users.size());
    assertEquals(3, users.stream().filter("user1"::equals).count());
    assertEquals(2, users.stream().filter("user2"::equals).count());
  }
}

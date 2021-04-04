package io.vertx.clickhousenativeclient;

import io.vertx.clickhouse.clickhousenative.ClickhouseNativeConnectOptions;
import io.vertx.clickhouse.clickhousenative.ClickhouseNativeConnection;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@RunWith(VertxUnitRunner.class)
public class SpecialTypesTest {
  @ClassRule
  public static ClickhouseResource rule = new ClickhouseResource();

  private ClickhouseNativeConnectOptions options;
  private Vertx vertx;

  @Before
  public void setup(TestContext ctx) {
    options = rule.options();
    vertx = Vertx.vertx();
  }

  @After
  public void teardDown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testNothing(TestContext ctx) {
    runQuery(ctx, "SELECT array()", null, null);
  }

  @Test
  public void testIntervalYear(TestContext ctx) {
    runQuery(ctx, "SELECT INTERVAL 4 YEAR", Duration.class, Optional.of(Duration.ofDays(365 * 4)));
  }

  @Test
  public void testIntervalQuarter(TestContext ctx) {
    runQuery(ctx, "SELECT INTERVAL 4 QUARTER", Duration.class, Optional.of(Duration.ofDays(120 * 4)));
  }

  @Test
  public void testIntervalMonth(TestContext ctx) {
    runQuery(ctx, "SELECT INTERVAL 4 MONTH", Duration.class, Optional.of(Duration.ofDays(30 * 4)));
  }

  @Test
  public void testIntervalWeek(TestContext ctx) {
    runQuery(ctx, "SELECT INTERVAL 4 WEEK", Duration.class, Optional.of(Duration.ofDays(7 * 4)));
  }

  @Test
  public void testIntervalWeekArray(TestContext ctx) {
    runQuery(ctx, "SELECT array(toIntervalWeek(4), toIntervalWeek(1), toIntervalWeek(0))", Duration[].class,
      Optional.of(new Duration[]{Duration.ofDays(4 * 7), Duration.ofDays(7), Duration.ofDays(0)}));
  }

  @Test
  //TODO smagellan: all other types from query "select * from system.data_type_families where name like 'Interval%';"
  public void testIntervalDay(TestContext ctx) {
    runQuery(ctx, "SELECT INTERVAL 4 DAY", Duration.class, Optional.of(Duration.ofDays(4)));
  }

  @Test
  public void testIntervalDayArray(TestContext ctx) {
    runQuery(ctx, "SELECT array(toIntervalDay(4), toIntervalDay(1), toIntervalDay(0))", Duration[].class,
      Optional.of(new Duration[]{Duration.ofDays(4), Duration.ofDays(1), Duration.ofDays(0)}));
  }

  @Test
  public void testIntervalHour(TestContext ctx) {
    runQuery(ctx, "SELECT INTERVAL 4 HOUR", Duration.class, Optional.of(Duration.ofHours(4)));
  }

  @Test
  public void testIntervalHourArray(TestContext ctx) {
    runQuery(ctx, "SELECT array(toIntervalHour(4), toIntervalHour(1), toIntervalHour(0))", Duration[].class,
      Optional.of(new Duration[]{Duration.ofHours(4), Duration.ofHours(1), Duration.ofHours(0)}));
  }

  @Test
  public void testIntervalMinute(TestContext ctx) {
    runQuery(ctx, "SELECT INTERVAL 4 MINUTE", Duration.class, Optional.of(Duration.ofMinutes(4)));
  }

  @Test
  public void testIntervalMinuteArray(TestContext ctx) {
    runQuery(ctx, "SELECT array(toIntervalMinute(4), toIntervalMinute(1), toIntervalMinute(0))", Duration[].class,
      Optional.of(new Duration[]{Duration.ofMinutes(4), Duration.ofMinutes(1), Duration.ofMinutes(0)}));
  }

  @Test
  public void testIntervalSecond(TestContext ctx) {
    runQuery(ctx, "SELECT INTERVAL 4 SECOND", Duration.class, Optional.of(Duration.ofSeconds(4)));
  }

  @Test
  public void testIntervalSecondArray(TestContext ctx) {
    runQuery(ctx, "SELECT array(toIntervalSecond(4), toIntervalSecond(1), toIntervalSecond(0))", Duration[].class,
      Optional.of(new Duration[]{Duration.ofSeconds(4), Duration.ofSeconds(1), Duration.ofSeconds(0)}));
  }

  private void runQuery(TestContext ctx, String query, Class<?> desiredCls, Optional<Object> expected) {
    ClickhouseNativeConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query(query).execute(
        ctx.asyncAssertSuccess(res -> {
          ctx.assertEquals(1, res.size());
          if (expected != null && expected.isPresent()) {
            Row row = res.iterator().next();
            Object val = desiredCls == null ? row.getValue(0) : row.get(desiredCls, 0);
            if (desiredCls.isArray()) {
              ctx.assertTrue(Arrays.deepEquals((Object[])expected.get(), (Object[])val));
            } else {
              ctx.assertEquals(expected.get(), val);
            }
          }
        }));
    }));
  }
}

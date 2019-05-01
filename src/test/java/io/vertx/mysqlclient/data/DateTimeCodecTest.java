package io.vertx.mysqlclient.data;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.mysqlclient.MySQLTestBase;
import io.vertx.pgclient.PgConnectOptions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;

public abstract class DateTimeCodecTest extends MySQLTestBase {
  Vertx vertx;
  PgConnectOptions options;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = new PgConnectOptions(MySQLTestBase.options);
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testDecodeAbbreviatedValue(TestContext ctx) {
    testDecodeGeneric(ctx, "11:12", "TIME", "test_time", Duration.ofHours(11).plusMinutes(12));
  }

  @Test
  public void testDecodeAbbreviatedValueWithoutColons(TestContext ctx) {
    testDecodeGeneric(ctx, "1112", "TIME", "test_time", Duration.ofMinutes(11).plusSeconds(12));
  }

  @Test
  public void testDecodeAbbreviatedValueWithoutColons2(TestContext ctx) {
    testDecodeGeneric(ctx, "12", "TIME", "test_time", Duration.ofSeconds(12));
  }

  @Test
  public void testDecodeMaxTime(TestContext ctx) {
    testDecodeGeneric(ctx, "838:59:59", "TIME", "test_time", Duration.ofHours(838).plusMinutes(59).plusSeconds(59));
  }

  @Test
  public void testDecodeMinTime(TestContext ctx) {
    testDecodeGeneric(ctx, "-838:59:59", "TIME", "test_time", Duration.ofHours(-838).plusMinutes(-59).plusSeconds(-59));
  }

  @Test
  public void testDecodeMaxTimeOverflow(TestContext ctx) {
    testDecodeGeneric(ctx, "850:00:00", "TIME", "test_time", Duration.ofHours(838).plusMinutes(59).plusSeconds(59));
  }

  @Test
  public void testDecodeMinTimeOverflow(TestContext ctx) {
    testDecodeGeneric(ctx, "-850:00:00", "TIME", "test_time", Duration.ofHours(-838).plusMinutes(-59).plusSeconds(-59));
  }

  @Test
  public void testDecodeDefaultFractionalSecondsPart(TestContext ctx) {
    testDecodeGeneric(ctx, "11:12:00.123456", "TIME", "test_time", Duration.ofHours(11).plusMinutes(12));
  }

  @Test
  public void testDecodeFractionalSecondsPart(TestContext ctx) {
    testDecodeGeneric(ctx, "11:12:00.123456", "TIME(6)", "test_time", Duration.ofHours(11).plusMinutes(12).plusNanos(123456000));
  }

  @Test
  public void testDecodeFractionalSecondsPartTruncation(TestContext ctx) {
    testDecodeGeneric(ctx, "11:12:00.123456", "TIME(4)", "test_time", Duration.ofHours(11).plusMinutes(12).plusNanos(123500000));
  }

  protected abstract <T> void testDecodeGeneric(TestContext ctx, String data, String dataType, String columnName, T expected);
}

package io.vertx.mysqlclient.data;

import io.vertx.ext.unit.TestContext;
import org.junit.Assume;
import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;

public abstract class DateTimeCodecTest extends MySQLDataTypeTestBase {
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
    Assume.assumeFalse(rule.isUsingMariaDB()); // MariaDB has not auto rounding for fractional seconds
    testDecodeGeneric(ctx, "11:12:00.123456", "TIME(4)", "test_time", Duration.ofHours(11).plusMinutes(12).plusNanos(123500000));
  }

  @Test
  public void testDecodeDatetime(TestContext ctx) {
    testDecodeGeneric(ctx, "2000-01-01 10:20:30", "DATETIME", "test_datetime", LocalDateTime.of(2000, 1, 1, 10, 20, 30));
  }

  @Test
  public void testDecodeDatetimeWithFractionalSeconds(TestContext ctx) {
    testDecodeGeneric(ctx, "2000-01-01 10:20:30.123456", "DATETIME(6)", "test_datetime", LocalDateTime.of(2000, 1, 1, 10, 20, 30, 123456000));
  }

  @Test
  public void testDecodeDatetimeWithFractionalSecondsTruncation(TestContext ctx) {
    Assume.assumeFalse(rule.isUsingMariaDB()); // MariaDB has not auto rounding for fractional seconds
    testDecodeGeneric(ctx, "2000-01-01 10:20:30.123456", "DATETIME(4)", "test_datetime", LocalDateTime.of(2000, 1, 1, 10, 20, 30, 123500000));
  }

  @Test
  public void testDecodeInvalidDatetime(TestContext ctx) {
    testDecodeGeneric(ctx, "2000-00-34 25:20:30", "DATETIME", "test_datetime", null);
  }

  protected abstract <T> void testDecodeGeneric(TestContext ctx, String data, String dataType, String columnName, T expected);
}

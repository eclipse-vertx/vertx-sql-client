package io.vertx.pgclient.data;

import io.vertx.pgclient.PgTestBase;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import org.junit.After;
import org.junit.Before;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
public abstract class DataTypeTestBase extends PgTestBase {

  protected Vertx vertx;

  protected static final UUID uuid = UUID.fromString("6f790482-b5bd-438b-a8b7-4a0bed747011");
  protected static final LocalDateTime ldt = LocalDateTime.parse("2017-05-14T19:35:58.237666");
  protected static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss.SSSSS");
  protected static final LocalTime lt = LocalTime.parse("17:55:04.90512", dtf);
  protected static final OffsetTime dt = OffsetTime.parse("17:55:04.90512+03:00");
  protected static final OffsetDateTime odt = OffsetDateTime.parse("2017-05-15T02:59:59.237666Z");
  protected static final Interval[] intervals = new Interval[] {
    Interval.of().years(10).months(3).days(332).hours(20).minutes(20).seconds(20).microseconds(999991),
    Interval.of().minutes(20).seconds(20).microseconds(123456),
    Interval.of().years(-2).months(-6)
  };

  @Before
  public void setup() throws Exception {
    super.setup();
    vertx = Vertx.vertx();
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }
}

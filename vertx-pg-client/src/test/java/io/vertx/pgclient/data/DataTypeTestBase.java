package io.vertx.pgclient.data;

import io.vertx.pgclient.PgTestBase;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.ColumnChecker;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.junit.After;
import org.junit.Before;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.vertx.sqlclient.ColumnChecker.*;

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

  static {
    ColumnChecker.load(() -> {
      List<ColumnChecker.SerializableBiFunction<Tuple, Integer, ?>> tupleMethods = new ArrayList<>();
      tupleMethods.add(Tuple::getValue);

      tupleMethods.add(Tuple::getShort);
      tupleMethods.add(Tuple::getInteger);
      tupleMethods.add(Tuple::getLong);
      tupleMethods.add(Tuple::getFloat);
      tupleMethods.add(Tuple::getDouble);
      tupleMethods.add(Tuple::getBigDecimal);
      tupleMethods.add(Tuple::getString);
      tupleMethods.add(Tuple::getBoolean);
      tupleMethods.add(Tuple::getJsonObject);
      tupleMethods.add(Tuple::getJsonArray);
      tupleMethods.add(Tuple::getBuffer);
      tupleMethods.add(Tuple::getBuffer);
      tupleMethods.add(Tuple::getTemporal);
      tupleMethods.add(Tuple::getLocalDate);
      tupleMethods.add(Tuple::getLocalTime);
      tupleMethods.add(Tuple::getOffsetTime);
      tupleMethods.add(Tuple::getLocalDateTime);
      tupleMethods.add(Tuple::getOffsetDateTime);
      tupleMethods.add(Tuple::getBooleanArray);
      tupleMethods.add(Tuple::getJsonObjectArray);
      tupleMethods.add(Tuple::getJsonArrayArray);
      tupleMethods.add(Tuple::getShortArray);
      tupleMethods.add(Tuple::getIntegerArray);
      tupleMethods.add(Tuple::getLongArray);
      tupleMethods.add(Tuple::getFloatArray);
      tupleMethods.add(Tuple::getDoubleArray);
      tupleMethods.add(Tuple::getNumericArray);
      tupleMethods.add(Tuple::getStringArray);
      tupleMethods.add(Tuple::getLocalDateArray);
      tupleMethods.add(Tuple::getLocalTimeArray);
      tupleMethods.add(Tuple::getOffsetTimeArray);
      tupleMethods.add(Tuple::getLocalDateTimeArray);
      tupleMethods.add(Tuple::getBufferArray);
      tupleMethods.add(Tuple::getUUIDArray);
      tupleMethods.add(getByIndex(Point.class));
      tupleMethods.add(getValuesByIndex(Point.class));
      tupleMethods.add(getValuesByIndex(Line.class));
      tupleMethods.add(getByIndex(Line.class));
      tupleMethods.add(getByIndex(LineSegment.class));
      tupleMethods.add(getValuesByIndex(LineSegment.class));
      tupleMethods.add(getByIndex(LineSegment.class));
      tupleMethods.add(getValuesByIndex(LineSegment.class));
      tupleMethods.add(getByIndex(Path.class));
      tupleMethods.add(getValuesByIndex(Path.class));
      tupleMethods.add(getByIndex(Polygon.class));
      tupleMethods.add(getValuesByIndex(Polygon.class));
      tupleMethods.add(getByIndex(Circle.class));
      tupleMethods.add(getValuesByIndex(Circle.class));
      return tupleMethods;
    }, () -> {
      List<ColumnChecker.SerializableBiFunction<Row, String, ?>> rowMethods = new ArrayList<>();
      rowMethods.add(Row::getValue);

      rowMethods.add(Row::getShort);
      rowMethods.add(Row::getInteger);
      rowMethods.add(Row::getLong);
      rowMethods.add(Row::getFloat);
      rowMethods.add(Row::getDouble);
      rowMethods.add(Row::getBigDecimal);
      rowMethods.add(Row::getString);
      rowMethods.add(Row::getBoolean);
      rowMethods.add(Row::getJsonObject);
      rowMethods.add(Row::getJsonArray);
      rowMethods.add(Row::getBuffer);
      rowMethods.add(Row::getBuffer);
      rowMethods.add(Row::getTemporal);
      rowMethods.add(Row::getLocalDate);
      rowMethods.add(Row::getLocalTime);
      rowMethods.add(Row::getOffsetTime);
      rowMethods.add(Row::getLocalDateTime);
      rowMethods.add(Row::getOffsetDateTime);
      rowMethods.add(Row::getBooleanArray);
      rowMethods.add(Row::getJsonObjectArray);
      rowMethods.add(Row::getJsonArrayArray);
      rowMethods.add(Row::getShortArray);
      rowMethods.add(Row::getIntegerArray);
      rowMethods.add(Row::getLongArray);
      rowMethods.add(Row::getFloatArray);
      rowMethods.add(Row::getDoubleArray);
      rowMethods.add(Row::getNumericArray);
      rowMethods.add(Row::getStringArray);
      rowMethods.add(Row::getLocalDateArray);
      rowMethods.add(Row::getLocalTimeArray);
      rowMethods.add(Row::getOffsetTimeArray);
      rowMethods.add(Row::getLocalDateTimeArray);
      rowMethods.add(Row::getBufferArray);
      rowMethods.add(Row::getUUIDArray);
      rowMethods.add(getByName(Point.class));
      rowMethods.add(getValuesByName(Point.class));
      rowMethods.add(getValuesByName(Line.class));
      rowMethods.add(getByName(Line.class));
      rowMethods.add(getByName(LineSegment.class));
      rowMethods.add(getValuesByName(LineSegment.class));
      rowMethods.add(getByName(LineSegment.class));
      rowMethods.add(getValuesByName(LineSegment.class));
      rowMethods.add(getByName(Path.class));
      rowMethods.add(getValuesByName(Path.class));
      rowMethods.add(getByName(Polygon.class));
      rowMethods.add(getValuesByName(Polygon.class));
      rowMethods.add(getByName(Circle.class));
      rowMethods.add(getValuesByName(Circle.class));

      return rowMethods;
    });
  }

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

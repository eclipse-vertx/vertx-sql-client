package io.vertx.clickhousenativeclient.alltypes;

import io.vertx.clickhouse.clickhousenative.impl.codec.columns.Decimal128Column;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.runner.RunWith;

import java.math.MathContext;
import java.math.RoundingMode;

@RunWith(VertxUnitRunner.class)
public class Decimal128Test extends HugeDecimalTest {
  public Decimal128Test() {
    super("decimal128", new MathContext(Decimal128Column.MAX_PRECISION, RoundingMode.HALF_EVEN));
  }
}

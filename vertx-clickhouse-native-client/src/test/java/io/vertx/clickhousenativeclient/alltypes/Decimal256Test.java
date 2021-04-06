package io.vertx.clickhousenativeclient.alltypes;

import io.vertx.clickhouse.clickhousenative.impl.codec.columns.Decimal256Column;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.runner.RunWith;

import java.math.MathContext;
import java.math.RoundingMode;

@RunWith(VertxUnitRunner.class)
public class Decimal256Test extends HugeDecimalTest {
  public Decimal256Test() {
    super("decimal256", new MathContext(Decimal256Column.MAX_PRECISION, RoundingMode.HALF_EVEN));
  }
}

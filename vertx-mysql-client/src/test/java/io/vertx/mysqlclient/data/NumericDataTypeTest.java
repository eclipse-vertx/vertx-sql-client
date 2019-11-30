package io.vertx.mysqlclient.data;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.data.Numeric;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class NumericDataTypeTest extends MySQLDataTypeTestBase {
  @Test
  public void testBinaryEncodeCastShortToDecimal(TestContext ctx) {
    testBinaryDecode(ctx, "SELECT * FROM basicdatatype WHERE id = 1 AND test_decimal = ?", Tuple.of((short) 12345), result -> {
      ctx.assertEquals(1, result.size());
      RowIterator<Row> iterator = result.iterator();
      Row row = iterator.next();
      ctx.assertEquals(1, row.getInteger("id"));
      ctx.assertEquals(Numeric.create(12345), row.getValue("test_decimal"));
    });
  }

  @Test
  public void testBinaryEncodeCastLongToShort(TestContext ctx) {
    testBinaryDecode(ctx, "SELECT * FROM basicdatatype WHERE id = 1 AND test_int_2 = ?", Tuple.of(32767L), result -> {
      ctx.assertEquals(1, result.size());
      RowIterator<Row> iterator = result.iterator();
      Row row = iterator.next();
      ctx.assertEquals(1, row.getInteger("id"));
      ctx.assertEquals((short) 32767, row.getValue("test_int_2"));
    });
  }
}

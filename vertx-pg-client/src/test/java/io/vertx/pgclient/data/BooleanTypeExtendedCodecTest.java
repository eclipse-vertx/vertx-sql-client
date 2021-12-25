package io.vertx.pgclient.data;

import io.vertx.pgclient.PgConnection;
import io.vertx.sqlclient.ColumnChecker;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public class BooleanTypeExtendedCodecTest extends ExtendedQueryDataTypeCodecTestBase {

  @Test
  public void testDecodeBoolean(TestContext ctx) {
    testDecode(ctx, "SELECT true", Tuple::getBoolean, true);
  }

  @Test
  public void testEncodeBoolean(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"NumericDataType\" SET \"Boolean\" = $1  WHERE \"id\" = $2 RETURNING \"Boolean\"",
        ctx.asyncAssertSuccess(p -> {
          p.query().execute(Tuple.tuple()
              .addBoolean(Boolean.FALSE)
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.rowCount());
              Row row = result.iterator().next();
              ColumnChecker.checkColumn(0, "Boolean")
                .returns(Tuple::getValue, Row::getValue, false)
                .returns(Tuple::getBoolean, Row::getBoolean, false)
                .forRow(row);
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeBooleanArray(TestContext ctx) {
    testDecode(ctx, "SELECT '{ true, false }'::BOOL[]", Tuple::getArrayOfBooleans, (Object)(new Boolean[] { true, false }));
  }

  @Test
  public void testDecodeBooleanArray_(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Boolean\" FROM \"ArrayDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.query().execute(Tuple.tuple()
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
            ColumnChecker.checkColumn(0, "Boolean")
              .returns(Tuple::getValue, Row::getValue, ColumnChecker.toObjectArray(new boolean[]{Boolean.TRUE}))
              .returns(Tuple::getArrayOfBooleans, Row::getArrayOfBooleans, ColumnChecker.toObjectArray(new boolean[]{Boolean.TRUE}))
              .forRow(result.iterator().next());
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeBooleanArray(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"Boolean\" = $1  WHERE \"id\" = $2 RETURNING \"Boolean\"",
        ctx.asyncAssertSuccess(p -> {
          p.query().execute(Tuple.tuple()
              .addArrayOfBoolean(new Boolean[]{Boolean.FALSE, Boolean.TRUE})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "Boolean")
                .returns(Tuple::getValue, Row::getValue, ColumnChecker.toObjectArray(new boolean[]{Boolean.FALSE, Boolean.TRUE}))
                .returns(Tuple::getArrayOfBooleans, Row::getArrayOfBooleans, ColumnChecker.toObjectArray(new boolean[]{Boolean.FALSE, Boolean.TRUE}))
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }
}

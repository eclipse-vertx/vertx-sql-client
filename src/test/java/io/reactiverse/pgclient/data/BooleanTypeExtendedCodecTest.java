package io.reactiverse.pgclient.data;

import io.reactiverse.pgclient.PgClient;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public class BooleanTypeExtendedCodecTest extends ExtendedQueryDataTypeCodecTestBase {
  @Test
  public void testDecodeBoolean(TestContext ctx) {
    testGeneric(ctx, "SELECT $1::BOOLEAN \"Boolean\"", new Boolean[]{true}, Tuple::getBoolean);
  }

  @Test
  public void testEncodeBoolean(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"NumericDataType\" SET \"Boolean\" = $1  WHERE \"id\" = $2 RETURNING \"Boolean\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
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
  public void testBooleanArray(TestContext ctx) {
    testGeneric(ctx,
      "SELECT c FROM (VALUES ($1 :: BOOL[])) AS t (c)",
      new Boolean[][]{new Boolean[]{true, null, false}}, Tuple::getBooleanArray);
  }

  @Test
  public void testDecodeBooleanArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Boolean\" FROM \"ArrayDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
            ColumnChecker.checkColumn(0, "Boolean")
              .returns(Tuple::getValue, Row::getValue, ColumnChecker.toObjectArray(new boolean[]{Boolean.TRUE}))
              .returns(Tuple::getBooleanArray, Row::getBooleanArray, ColumnChecker.toObjectArray(new boolean[]{Boolean.TRUE}))
              .forRow(result.iterator().next());
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeBooleanArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"Boolean\" = $1  WHERE \"id\" = $2 RETURNING \"Boolean\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
              .addBooleanArray(new Boolean[]{Boolean.FALSE, Boolean.TRUE})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "Boolean")
                .returns(Tuple::getValue, Row::getValue, ColumnChecker.toObjectArray(new boolean[]{Boolean.FALSE, Boolean.TRUE}))
                .returns(Tuple::getBooleanArray, Row::getBooleanArray, ColumnChecker.toObjectArray(new boolean[]{Boolean.FALSE, Boolean.TRUE}))
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }
}

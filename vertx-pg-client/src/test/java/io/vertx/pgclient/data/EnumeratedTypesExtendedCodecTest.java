package io.vertx.pgclient.data;

import io.vertx.pgclient.PgConnection;
import io.vertx.sqlclient.ColumnChecker;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public class EnumeratedTypesExtendedCodecTest extends ExtendedQueryDataTypeCodecTestBase {
  @Test
  public void testDecodeEnum(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"currentMood\" FROM \"EnumDataType\" WHERE \"id\" = $1").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p.query().execute(Tuple.tuple()
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            ColumnChecker.checkColumn(0, "currentMood")
              .returns(Tuple::getValue, Row::getValue, "ok")
              .returns(Tuple::getString, Row::getString, "ok")
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeEnum(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"EnumDataType\" SET \"currentMood\" = $1  WHERE \"id\" = $2 RETURNING \"currentMood\"").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p.query().execute(Tuple.tuple()
              .addString("happy")
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(1, result.rowCount());
              Row row = result.iterator().next();
              ColumnChecker.checkColumn(0, "currentMood")
                .returns(Tuple::getValue, Row::getValue, "happy")
                .returns(Tuple::getString, Row::getString, "happy")
                .forRow(row);
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testDecodeEnumArray(TestContext ctx) {
    final String[] expected = new String[]{"ok", "unhappy", "happy"};
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Enum\" FROM \"ArrayDataType\" WHERE \"id\" = $1").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p.query().execute(Tuple.tuple()
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
            ColumnChecker.checkColumn(0, "Enum")
              .returns(Tuple::getValue, Row::getValue, expected)
              .returns(Tuple::getArrayOfStrings, Row::getArrayOfStrings, expected)
              .forRow(result.iterator().next());
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeEnumArray(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"Enum\" = $1 WHERE \"id\" = $2 RETURNING \"Enum\"").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p.query().execute(Tuple.tuple()
              .addArrayOfString(new String[]{"unhappy"})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "Enum")
                .returns(Tuple::getValue, Row::getValue, new String[]{"unhappy"})
                .returns(Tuple::getArrayOfStrings, Row::getArrayOfStrings, new String[]{"unhappy"})
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testEncodeEnumArrayMultipleValues(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"Enum\" = $1 WHERE \"id\" = $2 RETURNING \"Enum\"").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p.query().execute(Tuple.tuple()
              .addArrayOfString(new String[]{"unhappy", "ok"})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "Enum")
                .returns(Tuple::getValue, Row::getValue, new String[]{"unhappy", "ok"})
                .returns(Tuple::getArrayOfStrings, Row::getArrayOfStrings, new String[]{"unhappy", "ok"})
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testEncodeEnumArrayEmptyValues(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"Enum\" = $1 WHERE \"id\" = $2 RETURNING \"Enum\").onComplete(\"Boolean\"").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p.query().execute(Tuple.tuple()
              .addArrayOfString(new String[]{})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "Enum")
                .returns(Tuple::getValue, Row::getValue, new String[]{})
                .returns(Tuple::getArrayOfStrings, Row::getArrayOfStrings, new String[]{})
                .forRow(result.iterator().next());
              ColumnChecker.checkColumn(1, "Boolean")
                .returns(Tuple::getValue, Row::getValue, new Boolean[]{true})
                .returns(Tuple::getArrayOfBooleans, Row::getArrayOfBooleans, new Boolean[]{true})
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }
}

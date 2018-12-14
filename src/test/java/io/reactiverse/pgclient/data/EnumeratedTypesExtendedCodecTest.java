package io.reactiverse.pgclient.data;

import io.reactiverse.pgclient.PgClient;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public class EnumeratedTypesExtendedCodecTest extends ExtendedQueryDataTypeCodecTestBase {
  @Test
  public void testDecodeEnum(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"currentMood\" FROM \"EnumDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
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
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"EnumDataType\" SET \"currentMood\" = $1  WHERE \"id\" = $2 RETURNING \"currentMood\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
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
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Enum\" FROM \"ArrayDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
            ColumnChecker.checkColumn(0, "Enum")
              .returns(Tuple::getValue, Row::getValue, expected)
              .returns(Tuple::getStringArray, Row::getStringArray, expected)
              .forRow(result.iterator().next());
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeEnumArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"Enum\" = $1 WHERE \"id\" = $2 RETURNING \"Enum\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
              .addStringArray(new String[]{"unhappy"})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "Enum")
                .returns(Tuple::getValue, Row::getValue, new String[]{"unhappy"})
                .returns(Tuple::getStringArray, Row::getStringArray, new String[]{"unhappy"})
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testEncodeEnumArrayMultipleValues(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"Enum\" = $1 WHERE \"id\" = $2 RETURNING \"Enum\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
              .addStringArray(new String[]{"unhappy", "ok"})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "Enum")
                .returns(Tuple::getValue, Row::getValue, new String[]{"unhappy", "ok"})
                .returns(Tuple::getStringArray, Row::getStringArray, new String[]{"unhappy", "ok"})
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }

  @Test
  public void testEncodeEnumArrayEmptyValues(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"Enum\" = $1 WHERE \"id\" = $2 RETURNING \"Enum\", \"Boolean\"",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
              .addStringArray(new String[]{})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "Enum")
                .returns(Tuple::getValue, Row::getValue, new String[]{})
                .returns(Tuple::getStringArray, Row::getStringArray, new String[]{})
                .forRow(result.iterator().next());
              ColumnChecker.checkColumn(1, "Boolean")
                .returns(Tuple::getValue, Row::getValue, new Boolean[]{true})
                .returns(Tuple::getBooleanArray, Row::getBooleanArray, new Boolean[]{true})
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }
}

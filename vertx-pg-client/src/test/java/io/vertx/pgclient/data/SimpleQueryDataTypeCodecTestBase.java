package io.vertx.pgclient.data;

import io.vertx.pgclient.PgConnection;
import io.vertx.sqlclient.ColumnChecker;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
public abstract class SimpleQueryDataTypeCodecTestBase extends DataTypeTestBase {

  @Override
  public void setup() throws Exception {
    super.setup();
    options.setCachePreparedStatements(false);
  }

  protected <T> void testDecodeGeneric(TestContext ctx,
                                       String data,
                                       String dataType,
                                       String columnName,
                                       Class<T> type,
                                       T expected) {
    testDecodeGeneric(ctx, data, dataType, columnName, ColumnChecker.getByIndex(type), ColumnChecker.getByName(type), expected);
  }

  protected <T> void testDecodeGeneric(TestContext ctx,
                                       String data,
                                       String dataType,
                                       String columnName,
                                       ColumnChecker.SerializableBiFunction<Tuple, Integer, T> byIndexGetter,
                                       ColumnChecker.SerializableBiFunction<Row, String, T> byNameGetter,
                                       T expected) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT '" + data + "' :: " + dataType + " \"" + columnName + "\"")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ColumnChecker.checkColumn(0, columnName)
          .returns(Tuple::getValue, Row::getValue, expected)
          .returns(byIndexGetter, byNameGetter, expected)
          .forRow(row);
        async.complete();
      }));
    }));
  }

  protected void testDecodeGenericArray(TestContext ctx,
                                        String arrayData,
                                        String columnName,
                                        Class<?> type,
                                        Object[] expected) {
    Class<Object> clazz = (Class<Object>) type;
    ColumnChecker.SerializableBiFunction<Tuple, Integer, Object> byIndex = ColumnChecker.getValuesByIndex(clazz);
    ColumnChecker.SerializableBiFunction<Row, String, Object> byName = ColumnChecker.getValuesByName(clazz);
    testDecodeGenericArray(ctx, arrayData, columnName, byIndex, byName, expected);
  }

  protected void testDecodeGenericArray(TestContext ctx,
                                        String arrayData,
                                        String columnName,
                                        ColumnChecker.SerializableBiFunction<Tuple, Integer, Object> byIndexGetter,
                                        ColumnChecker.SerializableBiFunction<Row, String, Object> byNameGetter,
                                        Object... expected) {
    testDecodeGenericArray(ctx, arrayData, columnName, ColumnChecker.checkColumn(0, columnName)
      .returns(Tuple::getValue, Row::getValue, expected)
      .returns(byIndexGetter, byNameGetter, expected));
  }

  protected void testDecodeGenericArray(TestContext ctx,
                                        String arrayData,
                                        String columnName,
                                        ColumnChecker checker) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SET TIME ZONE 'UTC'")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(res -> {
          conn
            .query("SELECT " + arrayData + " \"" + columnName + "\"")
            .execute()
            .onComplete(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            Row row = result.iterator().next();
            checker.forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  protected <T> void testDecodeXXXArray(TestContext ctx,
                                        String columnName,
                                        String tableName,
                                        ColumnChecker.SerializableBiFunction<Tuple, Integer, Object> byIndexGetter,
                                        ColumnChecker.SerializableBiFunction<Row, String, Object> byNameGetter,
                                        Object... expected) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SET TIME ZONE 'UTC'")
        .execute()
        .onComplete(
        ctx.asyncAssertSuccess(res -> {
          conn
            .query("SELECT \"" + columnName + "\" FROM \"" + tableName + "\" WHERE \"id\" = 1")
            .execute()
            .onComplete(
            ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, columnName)
                .returns(Tuple::getValue, Row::getValue, expected)
                .returns(byIndexGetter, byNameGetter, expected)
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }
}

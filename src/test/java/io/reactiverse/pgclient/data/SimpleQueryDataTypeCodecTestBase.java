package io.reactiverse.pgclient.data;

import io.reactiverse.pgclient.PgClient;
import io.reactiverse.pgclient.PgConnectOptions;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
public abstract class SimpleQueryDataTypeCodecTestBase extends DataTypeTestBase {

  @Override
  protected PgConnectOptions options() {
    return new PgConnectOptions(options).setCachePreparedStatements(false);
  }

  protected <T> void testDecodeGeneric(TestContext ctx,
                                       String data,
                                       String dataType,
                                       String columnName,
                                       ColumnChecker.SerializableBiFunction<Tuple, Integer, T> byIndexGetter,
                                       ColumnChecker.SerializableBiFunction<Row, String, T> byNameGetter,
                                       T expected) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT '" + data + "' :: " + dataType + " \"" + columnName + "\"", ctx.asyncAssertSuccess(result -> {
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
                                        ColumnChecker.SerializableBiFunction<Tuple, Integer, Object> byIndexGetter,
                                        ColumnChecker.SerializableBiFunction<Row, String, Object> byNameGetter,
                                        Object... expected) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SET TIME ZONE 'UTC'",
        ctx.asyncAssertSuccess(res -> {
          conn.query("SELECT " + arrayData + " \"" + columnName + "\"", ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            Row row = result.iterator().next();
            ColumnChecker.checkColumn(0, columnName)
              .returns(Tuple::getValue, Row::getValue, expected)
              .returns(byIndexGetter, byNameGetter, expected)
              .forRow(row);
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
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SET TIME ZONE 'UTC'",
        ctx.asyncAssertSuccess(res -> {
          conn.query("SELECT \"" + columnName + "\" FROM \"" + tableName + "\" WHERE \"id\" = 1",
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

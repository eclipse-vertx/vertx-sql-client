package com.julienviet.pgclient;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class DataTypeBinaryTest extends DataTypeTestBase {

  @Override
  protected PgClientOptions options() {
    return new PgClientOptions(options).setCachePreparedStatements(false);
  }

  @Test
  public void testBoolean(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Boolean\" FROM \"NumericDataType\" WHERE \"Boolean\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.query(Boolean.TRUE).execute(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.getNumRows());
            ctx.assertEquals(Boolean.TRUE, result.getResults().get(0).getBoolean(0));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testInt2(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Short\" FROM \"NumericDataType\" WHERE \"Short\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.query(Short.MAX_VALUE).execute(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.getNumRows());
            ctx.assertEquals(Short.MAX_VALUE, result.getResults().get(0).getValue(0));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testInt4(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Integer\" FROM \"NumericDataType\" WHERE \"Integer\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.query(Integer.MAX_VALUE).execute(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.getNumRows());
            ctx.assertEquals(Integer.MAX_VALUE, result.getResults().get(0).getInteger(0));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testInt8(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Long\" FROM \"NumericDataType\" WHERE \"Long\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.query(Long.MAX_VALUE).execute(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.getNumRows());
            ctx.assertEquals(Long.MAX_VALUE, result.getResults().get(0).getLong(0));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testFloat4(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Float\" FROM \"NumericDataType\" WHERE \"Float\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.query(Float.MAX_VALUE).execute(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.getNumRows());
            ctx.assertEquals(Float.MAX_VALUE, result.getResults().get(0).getFloat(0));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testFloat8(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Double\" FROM \"NumericDataType\" WHERE \"Double\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.query(Double.MAX_VALUE).execute(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.getNumRows());
            ctx.assertEquals(Double.MAX_VALUE, result.getResults().get(0).getDouble(0));
            async.complete();
          }));
        }));
    }));
  }
}

package com.julienviet.pgclient;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

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
          p.createQuery(Tuple.of(Boolean.TRUE)).execute(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(Boolean.TRUE, result.iterator().next().getBoolean(0));
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
          p.createQuery(Tuple.of(Short.MAX_VALUE)).execute(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(Short.MAX_VALUE, result.iterator().next().getValue(0));
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
          p.createQuery(Tuple.of(Integer.MAX_VALUE)).execute(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(Integer.MAX_VALUE, result.iterator().next().getInteger(0));
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
          p.createQuery(Tuple.of(Long.MAX_VALUE)).execute(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(Long.MAX_VALUE, result.iterator().next().getLong(0));
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
          p.createQuery(Tuple.of(Float.MAX_VALUE)).execute(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(Float.MAX_VALUE, result.iterator().next().getFloat(0));
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
          p.createQuery(Tuple.of(Double.MAX_VALUE)).execute(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(Double.MAX_VALUE, result.iterator().next().getDouble(0));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testDateBeforePgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Date\" FROM \"TemporalDataType\" WHERE \"Date\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.createQuery(Tuple.of("1981-05-30")).execute(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals("1981-05-30", result.iterator().next().getString(0));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testDateAfterPgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Date\" FROM \"TemporalDataType\" WHERE \"Date\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.createQuery(Tuple.of("2017-05-30")).execute(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals("2017-05-30", result.iterator().next().getString(0));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testTime(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Time\" FROM \"TemporalDataType\" WHERE \"Time\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.createQuery(Tuple.of("17:55:04.905120")).execute(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals("17:55:04.905120", result.iterator().next().getString(0));
            async.complete();
          }));
        }));
    }));
  }


  @Test
  public void testTimeTz(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"TimeTz\" FROM \"TemporalDataType\" WHERE \"TimeTz\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.createQuery(Tuple.of("17:55:04.90512+03:07")).execute(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals("17:55:04.905120+03:07", result.iterator().next().getString(0));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testTimestampBeforePgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Timestamp\" FROM \"TemporalDataType\" WHERE \"Timestamp\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.createQuery(Tuple.of(LocalDateTime.parse("1800-01-01T23:57:53.237666"))).execute(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(LocalDateTime.parse("1800-01-01T23:57:53.237666"), result.iterator().next().getTemporal(0));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testTimestampAfterPgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Timestamp\" FROM \"TemporalDataType\" WHERE \"Timestamp\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.createQuery(Tuple.of(LocalDateTime.parse("2017-05-14T19:35:58.237666"))).execute(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(LocalDateTime.parse("2017-05-14T19:35:58.237666"), result.iterator().next().getTemporal(0));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testTimestampTzBeforePgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.createQuery("SET TIME ZONE 'UTC'").execute(ctx.asyncAssertSuccess(v -> {
        conn.prepare("SELECT \"TimestampTz\" FROM \"TemporalDataType\" WHERE \"TimestampTz\" = $1",
          ctx.asyncAssertSuccess(p -> {
            p.createQuery(Tuple.of(OffsetDateTime.parse("1800-01-01T23:59:59.237666-03:00"))).execute(ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(OffsetDateTime.parse("1800-01-02T02:59:59.237666Z"), result.iterator().next().getTemporal(0));
              async.complete();
            }));
          }));
      }));
    }));
  }

  @Test
  public void testTimestampTzAfterPgEpoch(TestContext ctx) {
    Async async = ctx.async();
    PgClient client = PgClient.create(vertx, options);
    client.connect(ctx.asyncAssertSuccess(conn -> {
      conn.createQuery("SET TIME ZONE 'UTC'").execute(ctx.asyncAssertSuccess(v -> {
        conn.prepare("SELECT \"TimestampTz\" FROM \"TemporalDataType\" WHERE \"TimestampTz\" = $1",
          ctx.asyncAssertSuccess(p -> {
            p.createQuery(Tuple.of(OffsetDateTime.parse("2017-05-14T23:59:59.237666-03:00"))).execute(ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(OffsetDateTime.parse("2017-05-15T02:59:59.237666Z"), result.iterator().next().getTemporal(0));
              async.complete();
            }));
          }));
      }));
    }));
  }
}

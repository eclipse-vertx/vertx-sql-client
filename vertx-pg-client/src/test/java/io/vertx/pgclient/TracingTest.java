package io.vertx.pgclient;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.VertxTracerFactory;
import io.vertx.core.spi.tracing.TagExtractor;
import io.vertx.core.spi.tracing.VertxTracer;
import io.vertx.core.tracing.TracingOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.tracing.QueryRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class TracingTest extends PgTestBase {

  Vertx vertx;
  VertxTracer tracer;
  PgPool pool;

  @Before
  public void setup() throws Exception {
    super.setup();
    vertx = Vertx.vertx(new VertxOptions().setTracingOptions(
      new TracingOptions().setEnabled(true).setFactory(tracingOptions -> new VertxTracer() {
        @Override
        public Object sendRequest(Context context, Object request, String operation, BiConsumer headers, TagExtractor tagExtractor) {
          return tracer.sendRequest(context, request, operation, headers, tagExtractor);
        }

        @Override
        public void receiveResponse(Context context, Object response, Object payload, Throwable failure, TagExtractor tagExtractor) {
          tracer.receiveResponse(context, response, payload, failure, tagExtractor);
        }
      }))
    );
    pool = PgPool.pool(vertx, options, new PoolOptions());
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testTraceSimpleQuery(TestContext ctx) {
    String sql = "SELECT * FROM Fortune WHERE id=1";
    testTraceQuery(ctx, sql, Collections.emptyList(), conn -> conn.query(sql).execute());
  }

  @Test
  public void testTracePreparedQuery(TestContext ctx) {
    String sql = "SELECT * FROM Fortune WHERE id=$1";
    Tuple tuple = Tuple.of(1);
    testTraceQuery(ctx, sql, Collections.singletonList(tuple), conn -> conn.preparedQuery(sql).execute(tuple));
  }

  @Test
  public void testTraceBatchQuery(TestContext ctx) {
    String sql = "SELECT * FROM Fortune WHERE id=$1";
    List<Tuple> tuples = Arrays.asList(Tuple.of(1), Tuple.of(2));
    testTraceQuery(ctx, sql, tuples, conn -> conn.preparedQuery(sql).executeBatch(tuples));
  }

  public void testTraceQuery(TestContext ctx, String expectedSql, List<Tuple> expectedTuples, Function<SqlClient, Future<?>> fn) {
    AtomicBoolean called = new AtomicBoolean();
    AtomicReference<Context> requestContext = new AtomicReference<>();
    AtomicReference<Context> responseContext = new AtomicReference<>();
    Object expectedPayload = new Object();
    tracer = new VertxTracer<Object, Object>() {
      @Override
      public <R> Object sendRequest(Context context, R request, String operation, BiConsumer<String, String> headers, TagExtractor<R> tagExtractor) {
        QueryRequest query = (QueryRequest) request;
        ctx.assertEquals(expectedSql, query.sql());
        ctx.assertEquals(expectedTuples, query.tuples());
        Map<String, String> tags = tagExtractor.extract(request);
        ctx.assertEquals("client", tags.get("span.kind"));
        ctx.assertEquals("sql", tags.get("db.type"));
        ctx.assertEquals(expectedSql, tags.get("db.statement"));
        requestContext.set(context);
        return expectedPayload;
      }
      @Override
      public <R> void receiveResponse(Context context, R response, Object payload, Throwable failure, TagExtractor<R> tagExtractor) {
        RowSet rs = (RowSet) response;
        ctx.assertTrue(rs.iterator().hasNext());
        ctx.assertEquals(expectedPayload, payload);
        ctx.assertNull(failure);
        called.set(true);
        responseContext.set(context);
      }
    };
    Async async = ctx.async();
    vertx.runOnContext(v1 -> {
      Context context = Vertx.currentContext();
      pool.getConnection(ctx.asyncAssertSuccess(conn -> {
        fn.apply(conn).onComplete(ctx.asyncAssertSuccess(v2 -> {
          conn.close(ctx.asyncAssertSuccess(v3 -> {
            vertx.runOnContext(v4 -> {
              ctx.assertEquals(context, requestContext.get());
              ctx.assertEquals(context, responseContext.get());
              ctx.assertTrue(called.get());
              async.complete();
            });
          }));
        }));
      }));
    });
  }

  @Test
  public void testTracingFailure(TestContext ctx) {
    AtomicBoolean called = new AtomicBoolean();
    tracer = new VertxTracer<Object, Object>() {
      @Override
      public <R> Object sendRequest(Context context, R request, String operation, BiConsumer<String, String> headers, TagExtractor<R> tagExtractor) {
        return null;
      }
      @Override
      public <R> void receiveResponse(Context context, R response, Object payload, Throwable failure, TagExtractor<R> tagExtractor) {
        ctx.assertNull(response);
        ctx.assertNotNull(failure);
        called.set(true);
      }
    };
    pool.getConnection(ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery("SELECT 1 / $1")
        .execute(Tuple.of(0), ctx.asyncAssertFailure(err -> {
          ctx.assertTrue(called.get());
          conn.close();
        }));
    }));
  }

  @Test
  public void testMappingFailure(TestContext ctx) {
    RuntimeException failure = new RuntimeException();
    AtomicInteger called = new AtomicInteger();
    String sql = "SELECT * FROM Fortune WHERE id=$1";
    tracer = new VertxTracer<Object, Object>() {
      @Override
      public <R> Object sendRequest(Context context, R request, String operation, BiConsumer<String, String> headers, TagExtractor<R> tagExtractor) {
        return null;
      }
      @Override
      public <R> void receiveResponse(Context context, R response, Object payload, Throwable failure, TagExtractor<R> tagExtractor) {
        ctx.assertEquals(1, called.incrementAndGet());
      }
    };
    Async async = ctx.async();
    pool.getConnection(ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery(sql)
        .mapping(row -> {
          throw failure;
        })
        .execute(Tuple.of(1), ctx.asyncAssertFailure(err -> {
          conn.close(ctx.asyncAssertSuccess(v1 -> {
            vertx.runOnContext(v2 -> {
              ctx.assertEquals(1, called.get());
              async.complete();
            });
          }));
        }));
    }));
  }
}

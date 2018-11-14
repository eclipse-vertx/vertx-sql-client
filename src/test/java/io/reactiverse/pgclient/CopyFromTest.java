package io.reactiverse.pgclient;

import io.reactiverse.pgclient.codec.DataType;
import io.reactiverse.pgclient.copy.CopyData;
import io.reactiverse.pgclient.copy.CopyFromOptions;
import io.reactiverse.pgclient.copy.CopyTuple;
import io.reactiverse.pgclient.data.Interval;
import io.reactiverse.pgclient.data.Json;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class CopyFromTest extends PgTestBase {

  private enum Mood {
    happy
  }

  private static class EmptyReadStream implements ReadStream<Buffer> {

    private Handler<Void> endHandler;

    @Override
    public ReadStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
      return this;
    }

    @Override
    public ReadStream<Buffer> handler(@Nullable Handler<Buffer> handler) {
     if (handler != null) {
       endHandler.handle(null);
     }
      return this;
    }

    @Override
    public ReadStream<Buffer> pause() {
      return this;
    }

    @Override
    public ReadStream<Buffer> resume() {
      return this;
    }

    @Override
    public ReadStream<Buffer> endHandler(@Nullable Handler<Void> handler) {
      this.endHandler = handler;
      return this;
    }
  }

  private static class TupleReadStream implements ReadStream<CopyTuple> {

    private final Stream<CopyTuple> stream;
    private Handler<Void> endHandler;

    TupleReadStream(Stream<CopyTuple> stream) {
      this.stream = stream;
    }

    @Override
    public ReadStream<CopyTuple> exceptionHandler(Handler<Throwable> handler) {
      return this;
    }

    @Override
    public ReadStream<CopyTuple> handler(@Nullable Handler<CopyTuple> handler) {
      if (handler == null) {
        return this;
      }
      stream.forEach(handler::handle);
      endHandler.handle(null);
      return this;
    }

    @Override
    public ReadStream<CopyTuple> pause() {
      return this;
    }

    @Override
    public ReadStream<CopyTuple> resume() {
      return this;
    }

    @Override
    public ReadStream<CopyTuple> endHandler(@Nullable Handler<Void> handler) {
      endHandler = handler;
      return this;
    }
  }

  Vertx vertx;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testCopyFromEmptyStream(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {

      conn.copyFrom("CopyTable", new TupleReadStream(Stream.empty()), handler -> {
        if (handler.succeeded()) {
          ctx.assertEquals(0, handler.result());
          async.complete();
        } else {
          ctx.fail(handler.cause());
        }
      });
    }));
  }

  @Test
  public void testCopyFromText(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      vertx.fileSystem().open("copy/copy-text-input.txt", new OpenOptions(),
        ctx.asyncAssertSuccess(file -> {
          conn.copyFrom("CopyTable", file, new CopyFromOptions().setDelimiter(","), handler -> {
            if (handler.succeeded()) {
              ctx.assertEquals(2, handler.result());
              async.complete();
            } else {
              ctx.fail(handler.cause());
            }
          });
      }));
    }));
  }

  @Test
  public void testCopyFromCsv(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      vertx.fileSystem().open("copy/copy-csv-input.csv", new OpenOptions(),
        ctx.asyncAssertSuccess(file -> {
          conn.copyFrom("CopyTable", file, CopyFromOptions.csv(), handler -> {
            if (handler.succeeded()) {
              ctx.assertEquals(2, handler.result());
              async.complete();
            } else {
              ctx.fail(handler.cause());
            }
          });
        }));
    }));
  }

  @Test
  public void testCopyFromEmptyText(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
        conn.copyFrom("CopyTable", new EmptyReadStream(), new CopyFromOptions(), handler -> {
          if (handler.succeeded()) {
            ctx.assertEquals(0, handler.result());
            async.complete();
          } else {
            ctx.fail(handler.cause());
          }
        });
    }));
  }

  @Test
  public void testCopyFrom(TestContext ctx) {
    Async async = ctx.async();

    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      CopyTuple tuple = CopyTuple.of(
        CopyData.create((int) (Math.random() * 1000)),
        CopyData.create(true),
        CopyData.create((short) 1, DataType.INT2),
        CopyData.create(32),
        CopyData.create(300L),
        CopyData.create(300.3f),
        CopyData.create(10.50),
        CopyData.create("a", DataType.VARCHAR),
        CopyData.create("b"),
        CopyData.create("c"),
        CopyData.create("d"),
        CopyData.create(LocalDate.now()),
        CopyData.create(LocalTime.now()),
        CopyData.create(OffsetTime.now()),
        CopyData.create(LocalDateTime.now()),
        CopyData.create(OffsetDateTime.now()),
        CopyData.create(UUID.randomUUID()),
        CopyData.create(Buffer.buffer("hello"), DataType.BYTEA),
        CopyData.create(Json.create("{\"address\": {\"city\": \"AnyTown\"}}")),
        CopyData.create(Json.create("{\"address\": {\"street\": \"Main St\"}}"), DataType.JSONB),
        CopyData.create(Mood.happy),
        CopyData.create(new Interval())
      );

      conn.copyFrom("CopyTable", new TupleReadStream(Stream.of(tuple, tuple)), handler -> {
        if (handler.succeeded()) {
          ctx.assertEquals(2, handler.result());
          async.complete();
        } else {
          ctx.fail(handler.cause());
        }
      });
    }));
  }

  @Test
  public void testCopyFromMissingTuples(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      CopyTuple tuple = CopyTuple.of(
        CopyData.create((int) (Math.random() * 1000))
      );

      conn.copyFrom("CopyTable", new TupleReadStream(Stream.of(tuple, tuple)), handler -> {
        if (handler.succeeded()) {
          ctx.fail("Copy operation should have failed");
        }
        async.complete();
      });
    }));
  }

  @Test
  public void testCopyFromColumnNames(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      CopyTuple tuple = CopyTuple.of(
        CopyData.create("a", DataType.VARCHAR),
        CopyData.create(UUID.randomUUID()),
        CopyData.create(Json.create("{\"address\": {\"street\": \"Main St\"}}"), DataType.JSONB)
      );
      ArrayList<String> columns = new ArrayList<>();
      columns.add("Varchar");
      columns.add("UUID");
      columns.add("JSONB");
      conn.copyFrom("CopyTable", columns, new TupleReadStream(Stream.of(tuple, tuple)), handler -> {
        if (handler.succeeded()) {
          ctx.assertEquals(2, handler.result());
          async.complete();
        } else {
          ctx.fail(handler.cause());
        }
      });
    }));
  }
}

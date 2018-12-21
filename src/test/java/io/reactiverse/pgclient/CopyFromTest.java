package io.reactiverse.pgclient;

import io.reactiverse.pgclient.codec.DataType;
import io.reactiverse.pgclient.copy.CopyFromOptions;
import io.reactiverse.pgclient.copy.CopyTuple;
import io.reactiverse.pgclient.data.Box;
import io.reactiverse.pgclient.data.Circle;
import io.reactiverse.pgclient.data.Interval;
import io.reactiverse.pgclient.data.Json;
import io.reactiverse.pgclient.data.Line;
import io.reactiverse.pgclient.data.LineSegment;
import io.reactiverse.pgclient.data.Path;
import io.reactiverse.pgclient.data.Point;
import io.reactiverse.pgclient.data.Polygon;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.streams.Pump;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class CopyFromTest extends PgTestBase {

  private enum Mood {
    happy
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
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn ->
      conn.copyFrom("AllDataTypes", ctx.asyncAssertSuccess(writeStream -> {
        writeStream.endHandler(count -> {
          ctx.assertEquals(0, count);
          async.complete();
        });
        writeStream.exceptionHandler(ctx::fail);
        writeStream.end();
      }))));
  }

  @Test
  public void testCopyFromText(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      vertx.fileSystem().open("copy/copy-text-input.txt", new OpenOptions(),
          ctx.asyncAssertSuccess(file -> {
          conn.copyFrom("AllDataTypes", new CopyFromOptions().setDelimiter(","),
            ctx.asyncAssertSuccess(copyStream -> {
              Pump p = Pump.pump(file, copyStream);
              copyStream.endHandler(count -> {
                ctx.assertEquals(2, count);
                async.complete();
              });
              file.endHandler(v -> copyStream.end());
              copyStream.exceptionHandler(ctx::fail);
              p.start();
          }));
      }));
    }));
  }

  @Test
  public void testCopyFromCsv(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      vertx.fileSystem().open("copy/copy-csv-input.csv", new OpenOptions(),
        ctx.asyncAssertSuccess(file -> {
          conn.copyFrom("AllDataTypes", CopyFromOptions.csv(), ctx.asyncAssertSuccess(copyStream -> {
            Pump p = Pump.pump(file, copyStream);
            copyStream.endHandler(count -> {
              ctx.assertEquals(2, count);
              async.complete();
            });
            file.endHandler(v -> copyStream.end());
            copyStream.exceptionHandler(ctx::fail);
            p.start();
          }));
        }));
    }));
  }

  @Test
  public void testCopyFromEmptyText(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
        conn.copyFrom("AllDataTypes", new CopyFromOptions(), ctx.asyncAssertSuccess(copyStream -> {
          copyStream.endHandler(rows -> {
            ctx.assertEquals(0, rows);
            async.complete();
          });
          copyStream.exceptionHandler(ctx::fail);
          copyStream.end();
        }));
    }));
  }

  @Test
  public void testCopyFrom(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      CopyTuple t = CopyTuple.tuple();
      t.addBoolean(true);
      t.addShort((short) 1);
      t.addInteger(32);
      t.addLong(300L);
      t.addFloat(300.3f);
      t.addDouble(10.50);
      t.addValue("a", DataType.CHAR);
      t.addVarChar("a");
      t.addString("b");
      t.addValue(Mood.happy);
      t.addValue("c", DataType.NAME);
      t.addUUID(UUID.randomUUID());
      t.addLocalDate(LocalDate.now());
      t.addLocalTime(LocalTime.now());
      t.addOffsetTime(OffsetTime.now());
      t.addLocalDateTime(LocalDateTime.now());
      t.addOffsetDateTime(OffsetDateTime.now());
      t.addInterval(new Interval());
      t.addBuffer(Buffer.buffer("hello"));
      t.addJson(Json.create("{\"address\": {\"city\": \"AnyTown\"}}"));
      t.addJsonb(Json.create("{\"address\": {\"city\": \"AnyTown\"}}"));
      t.addPoint(new Point());
      t.addLine(new Line(10, 20, 30));
      t.addLineSegment((new LineSegment()));
      t.addBox(new Box());
      t.addPath(new Path(true, Collections.singletonList(new Point(1, 1))));
      t.addPolygon(new Polygon(Collections.singletonList(new Point(1, 1))));
      t.addCircle(new Circle());
        
      ArrayList<String> columns = new ArrayList<>(Arrays.asList(
        "boolean",
        "int2",
        "int4",
        "int8",
        "float4",
        "float8",
        "char",
        "varchar",
        "text",
        "enum",
        "name",
        // "numeric", TODO numeric binary encoding support
        "uuid",
        "date",
        "time",
        "timetz",
        "timestamp",
        "timestamptz",
        "interval",
        "bytea",
        "json",
        "jsonb",
        "point",
        "line",
        "lseg",
        "box",
        "path",
        "polygon",
        "circle"
      ));


   conn.copyFrom("AllDataTypes",  columns, ctx.asyncAssertSuccess(copyStream -> {
        copyStream.endHandler(count -> {
          ctx.assertEquals(2, count);
          async.complete();
        });
        copyStream.exceptionHandler(ctx::fail);
        copyStream.write(t);
        copyStream.end(t);
      }));
    }));
  }

  @Test
  public void testCopyFromMissingTuples(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      CopyTuple tuple = CopyTuple.tuple();
      tuple.addBoolean(true);

      conn.copyFrom("AllDataTypes", ctx.asyncAssertSuccess(copyStream -> {
        copyStream.endHandler(count -> {
          ctx.fail("Copy operation should have failed");
        });
        copyStream.exceptionHandler(t -> async.complete());
        copyStream.end(tuple);
      }));
    }));
  }
}

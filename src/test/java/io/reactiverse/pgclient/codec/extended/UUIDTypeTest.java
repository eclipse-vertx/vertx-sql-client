package io.reactiverse.pgclient.codec.extended;

import io.reactiverse.pgclient.PgClient;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.reactiverse.pgclient.codec.ColumnChecker;
import io.reactiverse.pgclient.codec.ExtendedQueryDataTypeCodecTestBase;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.UUID;

public class UUIDTypeTest extends ExtendedQueryDataTypeCodecTestBase {
  @Test
  public void testDecodeUUID(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"uuid\" FROM \"CharacterDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            ColumnChecker.checkColumn(0, "uuid")
              .returns(Tuple::getValue, Row::getValue, uuid)
              .returns(Tuple::getUUID, Row::getUUID, uuid)
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeUUID(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"CharacterDataType\" SET \"uuid\" = $1 WHERE \"id\" = $2 RETURNING \"uuid\"",
        ctx.asyncAssertSuccess(p -> {
          UUID uuid = UUID.fromString("92b53cf1-2ad0-49f9-be9d-ca48966e43ee");
          p.execute(Tuple.tuple()
            .addUUID(uuid)
            .addInteger(2), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            ColumnChecker.checkColumn(0, "uuid")
              .returns(Tuple::getValue, Row::getValue, uuid)
              .returns(Tuple::getUUID, Row::getUUID, uuid)
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testDecodeUUIDArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"UUID\" FROM \"ArrayDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple()
            .addInteger(1), ctx.asyncAssertSuccess(result -> {
            final UUID uuid = UUID.fromString("6f790482-b5bd-438b-a8b7-4a0bed747011");
            ColumnChecker.checkColumn(0, "UUID")
              .returns(Tuple::getValue, Row::getValue, new UUID[]{uuid})
              .returns(Tuple::getUUIDArray, Row::getUUIDArray, new UUID[]{uuid})
              .forRow(result.iterator().next());
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeUUIDArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"UUID\" = $1  WHERE \"id\" = $2 RETURNING \"UUID\"",
        ctx.asyncAssertSuccess(p -> {
          final UUID uuid = UUID.fromString("6f790482-b5bd-438b-a8b7-4a0bed747011");
          p.execute(Tuple.tuple()
              .addUUIDArray(new UUID[]{uuid})
              .addInteger(2)
            , ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "UUID")
                .returns(Tuple::getValue, Row::getValue, new UUID[]{uuid})
                .returns(Tuple::getUUIDArray, Row::getUUIDArray, new UUID[]{uuid})
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }
}

/*
 * Copyright (C) 2020 IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertx.db2client;

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.RowId;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assume.assumeTrue;

@RunWith(VertxUnitRunner.class)
public class DB2DataTypeTest extends DB2TestBase {


  // Enum for enum testing
  enum Days {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
  }


  /**
   * In DB2 the FLOAT and DOUBLE column types both map to an 8-byte
   * double-precision column (i.e. Java double). Ensure that a Java
   * float can still be inserted and selected from such a column
   */
  @Test
  public void testFloatIntoFloatColumn(TestContext ctx) {
      connect(ctx.asyncAssertSuccess(conn -> {
          conn.preparedQuery("INSERT INTO db2_types (id,test_float) VALUES (?, ?)")
            .execute(Tuple.of(1, 5.0f), ctx.asyncAssertSuccess(insertResult -> {
               conn.preparedQuery("SELECT id,test_float FROM db2_types WHERE id = 1")
                 .execute(ctx.asyncAssertSuccess(rows -> {
                   ctx.assertEquals(1, rows.size());
                   Row row = rows.iterator().next();
                   ctx.assertEquals(1, row.getInteger(0));
                   ctx.assertEquals(5.0f, row.getFloat(1));
                 }));
            }));
        }));
  }

  /**
   * DB2 has no BYTE or BOOLEAN column type, and instead maps it to a
   * 2-byte SMALLINT column type. This means Java byte types must be
   * converted into SMALLINT formats
   */
  @Test
  public void testByteIntoSmallIntColumn(TestContext ctx) {
      connect(ctx.asyncAssertSuccess(conn -> {
          conn.preparedQuery("INSERT INTO db2_types (id,test_byte) VALUES (?, ?)")
            .execute(Tuple.of(2, (byte) 0xCA), ctx.asyncAssertSuccess(insertResult -> {
               conn.preparedQuery("SELECT id,test_byte FROM db2_types WHERE id = 2")
                 .execute(ctx.asyncAssertSuccess(rows -> {
                   ctx.assertEquals(1, rows.size());
                   Row row = rows.iterator().next();
                   ctx.assertEquals(2, row.getInteger(0));
                   ctx.assertEquals((byte) 0xCA, row.get(Byte.class, 1));
                 }));
            }));
        }));
  }

  @Test
  public void testByteArrayIntoChar(TestContext ctx) {
    byte[] param = "hello world".getBytes();
    byte[] expected = new byte[255];
    Arrays.fill(expected, (byte) 32);
    System.arraycopy(param, 0, expected, 0, param.length);
    testByteArrayInto(ctx, "test_bytes", param, expected);
  }

  @Test
  public void testByteArrayIntoVarchar(TestContext ctx) {
    byte[] param = "hello world".getBytes();
    testByteArrayInto(ctx, "test_vbytes", param, param);
  }

  private void testByteArrayInto(TestContext ctx, String colName, byte[] param, byte[] expected) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery("INSERT INTO db2_types (id," + colName + ") VALUES (?, ?)")
        .execute(Tuple.of(3, param))
        .onComplete(ctx.asyncAssertSuccess(insertResult -> {
          conn
            .preparedQuery("SELECT id," + colName + " FROM db2_types WHERE id = 3")
            .execute()
            .onComplete(ctx.asyncAssertSuccess(rows -> {
              ctx.assertEquals(1, rows.size());
              Row row = rows.iterator().next();
              ctx.assertEquals(3, row.getInteger(0));
              ctx.assertTrue(Arrays.equals(expected, row.getBuffer(1).getBytes()),
                "Expecting " + Arrays.toString(expected) + " but got "
                  + Arrays.toString(row.getBuffer(1).getBytes()));
            }));
        }));
    }));
  }

  @Test
  public void testBufferIntoChar(TestContext ctx) {
    byte[] param = "hello world".getBytes();
    byte[] expected = new byte[255];
    Arrays.fill(expected, (byte) 32);
    System.arraycopy(param, 0, expected, 0, param.length);
    testBufferInto(ctx, "test_bytes", param, expected);
  }

  @Test
  public void testBufferIntoVarchar(TestContext ctx) {
    byte[] param = "hello world".getBytes();
    testBufferInto(ctx, "test_vbytes", param, param);
  }

  private void testBufferInto(TestContext ctx, String colName, byte[] param, byte[] expected) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery("INSERT INTO db2_types (id," + colName + ") VALUES (?, ?)")
        .execute(Tuple.of(4, Buffer.buffer(param)))
        .onComplete(ctx.asyncAssertSuccess(insertResult -> {
          conn
            .preparedQuery("SELECT id," + colName + " FROM db2_types WHERE id = 4")
            .execute()
            .onComplete(ctx.asyncAssertSuccess(rows -> {
              ctx.assertEquals(1, rows.size());
              Row row = rows.iterator().next();
              ctx.assertEquals(4, row.getInteger(0));
              ctx.assertTrue(Arrays.equals(expected, row.getBuffer(1).getBytes()),
                "Expecting " + Arrays.toString(expected) + " but got "
                  + Arrays.toString(row.getBuffer(1).getBytes()));
            }));
        }));
    }));
  }

  @Test
  public void testTimestamp(TestContext ctx) {
    LocalDateTime now = LocalDateTime.now();
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("INSERT INTO db2_types (id,test_tstamp) VALUES (?,?)")
        .execute(Tuple.of(5, now), ctx.asyncAssertSuccess(insertResult -> {
           conn.preparedQuery("SELECT id,test_tstamp FROM db2_types WHERE id = ?")
             .execute(Tuple.of(5), ctx.asyncAssertSuccess(rows -> {
          ctx.assertEquals(1, rows.size());
          Row row = rows.iterator().next();
          int nowNanos = now.getNano() - (1000 * now.get(ChronoField.MICRO_OF_SECOND));
          int dbNanos = row.getLocalDateTime(1).getNano() - (1000 * row.getLocalDateTime(1).get(ChronoField.MICRO_OF_SECOND));
          ctx.assertEquals(5, row.getInteger(0));
          ctx.assertEquals(dbNanos > 0 ? now : now.minusNanos(nowNanos), row.getLocalDateTime(1));
             }));
        }));
    }));
  }

  @Test
  public void testUUID(TestContext ctx) {
    UUID uuid = UUID.randomUUID();
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("INSERT INTO db2_types (id,test_vchar) VALUES (?,?)").execute(Tuple.of(6, uuid),
          ctx.asyncAssertSuccess(insertResult -> {
            conn.preparedQuery("SELECT id,test_vchar FROM db2_types WHERE id = ?").execute(Tuple.of(6),
                ctx.asyncAssertSuccess(rows -> {
                  ctx.assertEquals(1, rows.size());
                  Row row = rows.iterator().next();
                  ctx.assertEquals(6, row.getInteger(0));
                  ctx.assertEquals(uuid, row.getUUID(1));
                  ctx.assertEquals(uuid, row.getUUID("test_vchar"));
                  ctx.assertEquals(uuid, row.get(UUID.class, 1));
                }));
          }));
    }));
  }

  @Test
  public void testRowId(TestContext ctx) {
    assumeTrue("Only DB2/Z supports the ROWID column type", rule.isZOS());

    final String msg = "insert data for testRowId";
    connect(ctx.asyncAssertSuccess(conn -> {
      // Insert some data
      conn.preparedQuery("INSERT INTO ROWTEST (message) VALUES ('" + msg + "')")
        .execute(ctx.asyncAssertSuccess(insertResult -> {
           // Find it by msg
           conn.preparedQuery("SELECT * FROM ROWTEST WHERE message = '" + msg + "'")
             .execute(ctx.asyncAssertSuccess(rows -> {
               RowId rowId = verifyRowId(ctx, rows, msg);
               // Now find it by rowid
               conn.preparedQuery("SELECT * FROM ROWTEST WHERE id = ?")
                 .execute(Tuple.of(rowId), ctx.asyncAssertSuccess(rows2 -> {
                   verifyRowId(ctx, rows2, msg);
                 }));
             }));
        }));
    }));
  }

  /**
   * Test to support using enum string values in the Row and Tuple methods.
   */
  @Test
  public void testUsingEnumNameValue(TestContext ctx) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("INSERT INTO db2_types (id,test_vchar) VALUES (?, ?)")
       .execute(Tuple.of(10, Days.WEDNESDAY), ctx.asyncAssertSuccess(insertResult -> {
         conn.preparedQuery("SELECT id,test_vchar FROM db2_types WHERE id = 10")
          .execute(ctx.asyncAssertSuccess(rows -> {
           ctx.assertEquals(1, rows.size());
           Row row = rows.iterator().next();
           ctx.assertEquals(10, row.getInteger(0));
           ctx.assertEquals(Days.WEDNESDAY, row.get(Days.class, 1));
          }));
       }));
     }));
  }

  /**
   * Test to support using enum ordinal values in the Row and Tuple methods.
   */
  @Test
  public void testUsingEnumOrdinalValue(TestContext ctx) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("INSERT INTO db2_types (id,test_int) VALUES (?, ?)")
       .execute(Tuple.of(11, Days.FRIDAY.ordinal()), ctx.asyncAssertSuccess(insertResult -> {
         conn.preparedQuery("SELECT id,test_int FROM db2_types WHERE id = 11")
          .execute(ctx.asyncAssertSuccess(rows -> {
           ctx.assertEquals(1, rows.size());
           Row row = rows.iterator().next();
           ctx.assertEquals(11, row.getInteger(0));
           ctx.assertEquals(Days.FRIDAY, row.get(Days.class, 1));
          }));
       }));
     }));
  }

  private RowId verifyRowId(TestContext ctx, RowSet<Row> rows, String msg) {
    ctx.assertEquals(1, rows.size());
    Row row = rows.iterator().next();
    ctx.assertEquals(msg, row.getString(1));
    RowId rowid = row.get(RowId.class, 0);
    ctx.assertNotNull(rowid);
    ctx.assertEquals(22, rowid.getBytes().length);
    return rowid;
  }

  @Override
  protected List<String> tablesToClean() {
    return Collections.singletonList("db2_types");
  }
}

/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mysqlclient;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.Tuple;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@RunWith(VertxUnitRunner.class)
public class MySQLQueryTest extends MySQLTestBase {

  Vertx vertx;
  MySQLConnectOptions options;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = new MySQLConnectOptions(MySQLTestBase.options);
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testLastInsertIdWithDefaultValue(TestContext ctx) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("CREATE TEMPORARY TABLE last_insert_id(id INTEGER PRIMARY KEY AUTO_INCREMENT, val VARCHAR(20));").execute(ctx.asyncAssertSuccess(createTableResult -> {
        long lastInsertId1 = createTableResult.property(MySQLClient.LAST_INSERTED_ID);
        ctx.assertEquals(0L, lastInsertId1);
        conn.query("INSERT INTO last_insert_id(val) VALUES('test')").execute(ctx.asyncAssertSuccess(insertResult1 -> {
          long lastInsertId2 = insertResult1.property(MySQLClient.LAST_INSERTED_ID);
          ctx.assertEquals(1L, lastInsertId2);
          conn.query("INSERT INTO last_insert_id(val) VALUES('test2')").execute(ctx.asyncAssertSuccess(insertResult2 -> {
            long lastInsertId3 = insertResult2.property(MySQLClient.LAST_INSERTED_ID);
            ctx.assertEquals(2L, lastInsertId3);
            conn.close();
          }));
        }));
      }));
    }));
  }

  @Test
  public void testLastInsertIdWithSpecifiedValue(TestContext ctx) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("CREATE TEMPORARY TABLE last_insert_id(id INTEGER PRIMARY KEY AUTO_INCREMENT, val VARCHAR(20));").execute(ctx.asyncAssertSuccess(createTableResult -> {
        long lastInsertId1 = createTableResult.property(MySQLClient.LAST_INSERTED_ID);
        ctx.assertEquals(0L, lastInsertId1);
        conn.query("ALTER TABLE last_insert_id AUTO_INCREMENT=1234").execute(ctx.asyncAssertSuccess(alterTableResult -> {
          long lastInsertId2 = createTableResult.property(MySQLClient.LAST_INSERTED_ID);
          ctx.assertEquals(0L, lastInsertId2);
          conn.query("INSERT INTO last_insert_id(val) VALUES('test')").execute(ctx.asyncAssertSuccess(insertResult1 -> {
            long lastInsertId3 = insertResult1.property(MySQLClient.LAST_INSERTED_ID);
            ctx.assertEquals(1234L, lastInsertId3);
            conn.query("INSERT INTO last_insert_id(val) VALUES('test2')").execute(ctx.asyncAssertSuccess(insertResult2 -> {
              long lastInsertId4 = insertResult2.property(MySQLClient.LAST_INSERTED_ID);
              ctx.assertEquals(1235L, lastInsertId4);
              conn.close();
            }));
          }));
        }));
      }));
    }));
  }

  @Test
  public void testCachePreparedStatementWithDifferentSql(TestContext ctx) {
    // we set the cache size to be the same with max_prepared_stmt_count
    MySQLConnection.connect(vertx, options.setCachePreparedStatements(true)
      .setPreparedStatementCacheMaxSize(16382), ctx.asyncAssertSuccess(conn -> {
      conn.query("SHOW VARIABLES LIKE 'max_prepared_stmt_count'").execute(ctx.asyncAssertSuccess(res1 -> {
        Row row = res1.iterator().next();
        int maxPreparedStatementCount = Integer.parseInt(row.getString(1));
        ctx.assertEquals("max_prepared_stmt_count", row.getString(0));
        ctx.assertEquals(16382, maxPreparedStatementCount);

        for (int i = 0; i < 10000; i++) {
          String randomString = UUID.randomUUID().toString();
          for (int j = 0; j < 2; j++) {
            conn.preparedQuery("SELECT '" + randomString + "'").execute(ctx.asyncAssertSuccess(res2 -> {
              ctx.assertEquals(randomString, res2.iterator().next().getString(0));
            }));
          }
        }
      }));
    }));
  }

  @Test
  public void testCachePreparedStatementWithSameSql(TestContext ctx) {
    MySQLConnection.connect(vertx, options.setCachePreparedStatements(true), ctx.asyncAssertSuccess(conn -> {
      conn.query("SHOW VARIABLES LIKE 'max_prepared_stmt_count'").execute(ctx.asyncAssertSuccess(res1 -> {
        Row row = res1.iterator().next();
        int maxPreparedStatementCount = Integer.parseInt(row.getString(1));
        ctx.assertEquals("max_prepared_stmt_count", row.getString(0));
        ctx.assertEquals(16382, maxPreparedStatementCount);

        for (int i = 0; i < 20000; i++) {
          conn.preparedQuery("SELECT 'test'").execute(ctx.asyncAssertSuccess(res2 -> {
            ctx.assertEquals("test", res2.iterator().next().getString(0));
          }));
        }
      }));
    }));
  }

  @Test
  public void testDecodePacketSizeMoreThan16MB(TestContext ctx) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 4000000; i++) {
      sb.append("abcde");
    }
    String expected = sb.toString();

    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT REPEAT('abcde', 4000000)").execute(ctx.asyncAssertSuccess(rowSet -> {
        ctx.assertEquals(1, rowSet.size());
        Row row = rowSet.iterator().next();
        ctx.assertTrue(row.getString(0).getBytes().length > 0xFFFFFF);
        ctx.assertEquals(expected, row.getValue(0));
        ctx.assertEquals(expected, row.getString(0));
        conn.close();
      }));
    }));
  }

  @Test
  public void testEncodePacketSizeMoreThan16MB(TestContext ctx) {
    Assume.assumeFalse(rule.isUsingMySQL5_6());
    int dataSize = 20 * 1024 * 1024; // 20MB payload
    byte[] data = new byte[dataSize];
    ThreadLocalRandom.current().nextBytes(data);
    Buffer buffer = Buffer.buffer(data);
    ctx.assertTrue(buffer.length() > 0xFFFFFF);

    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("UPDATE datatype SET `LongBlob` = ? WHERE id = 2").execute(Tuple.of(buffer), ctx.asyncAssertSuccess(v -> {
        conn.preparedQuery("SELECT id, `LongBlob` FROM datatype WHERE id = 2").execute(ctx.asyncAssertSuccess(rowSet -> {
          Row row = rowSet.iterator().next();
          ctx.assertEquals(2, row.getInteger(0));
          ctx.assertEquals(2, row.getInteger("id"));
          ctx.assertEquals(buffer, row.getBuffer(1));
          ctx.assertEquals(buffer, row.getBuffer("LongBlob"));
          conn.close();
        }));
      }));
    }));
  }

  @Test
  public void testLocalInfileRequest(TestContext ctx) {
    FileSystem fileSystem = vertx.fileSystem();
    Buffer fileData = Buffer.buffer().appendString("Fluffy,Harold,cat,f,1993-02-04,NULL")
      .appendString("\n")
      .appendString("Bowser,Diane,dog,m,1979-08-31,1995-07-29")
      .appendString("\n")
      .appendString("Whistler,Gwen,bird,NULL,1997-12-09,NULL");
    fileSystem.createTempFile(null, null, ctx.asyncAssertSuccess(filename -> {
      fileSystem.writeFile(filename, fileData, ctx.asyncAssertSuccess(write -> {
        MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
          conn.query("TRUNCATE TABLE localinfile").execute(ctx.asyncAssertSuccess(cleanup -> {
            conn.query("LOAD DATA LOCAL INFILE '" + filename + "' INTO TABLE localinfile FIELDS TERMINATED BY ',' ENCLOSED BY '\"' LINES TERMINATED BY '\\n';").execute(ctx.asyncAssertSuccess(v -> {
              conn.query("SELECT * FROM localinfile").execute(ctx.asyncAssertSuccess(rowSet -> {
                ctx.assertEquals(3, rowSet.size());
                RowIterator<Row> iterator = rowSet.iterator();
                Row row1 = iterator.next();
                ctx.assertEquals("Fluffy", row1.getValue(0));
                ctx.assertEquals("Harold", row1.getValue(1));
                ctx.assertEquals("cat", row1.getValue(2));
                ctx.assertEquals("f", row1.getValue(3));
                ctx.assertEquals(LocalDate.of(1993, 2, 4), row1.getValue(4));
                ctx.assertEquals(null, row1.getValue(5));
                Row row2 = iterator.next();
                ctx.assertEquals("Bowser", row2.getValue(0));
                ctx.assertEquals("Diane", row2.getValue(1));
                ctx.assertEquals("dog", row2.getValue(2));
                ctx.assertEquals("m", row2.getValue(3));
                ctx.assertEquals(LocalDate.of(1979, 8, 31), row2.getValue(4));
                ctx.assertEquals(LocalDate.of(1995, 7, 29), row2.getValue(5));
                Row row3 = iterator.next();
                ctx.assertEquals("Whistler", row3.getValue(0));
                ctx.assertEquals("Gwen", row3.getValue(1));
                ctx.assertEquals("bird", row3.getValue(2));
                ctx.assertEquals(null, row3.getValue(3));
                ctx.assertEquals(LocalDate.of(1997, 12, 9), row3.getValue(4));
                ctx.assertEquals(null, row3.getValue(5));
                conn.close();
              }));
            }));
          }));
        }));
      }));
    }));
  }
}

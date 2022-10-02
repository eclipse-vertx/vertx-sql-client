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
import io.vertx.sqlclient.*;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    // Test with our own property
    PropertyKind<Long> property = PropertyKind.create("last-inserted-id", Long.class);
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("CREATE TEMPORARY TABLE last_insert_id(id INTEGER PRIMARY KEY AUTO_INCREMENT, val VARCHAR(20));").execute(ctx.asyncAssertSuccess(createTableResult -> {
        Long lastInsertId1 = createTableResult.property(property);
        ctx.assertNull(lastInsertId1);
        conn.query("INSERT INTO last_insert_id(val) VALUES('test')").execute(ctx.asyncAssertSuccess(insertResult1 -> {
          Long lastInsertId2 = insertResult1.property(property);
          ctx.assertEquals(1L, lastInsertId2);
          conn.query("INSERT INTO last_insert_id(val) VALUES('test2')").execute(ctx.asyncAssertSuccess(insertResult2 -> {
            Long lastInsertId3 = insertResult2.property(property);
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
        Long lastInsertId1 = createTableResult.property(MySQLClient.LAST_INSERTED_ID);
        ctx.assertNull(lastInsertId1);
        conn.query("ALTER TABLE last_insert_id AUTO_INCREMENT=1234").execute(ctx.asyncAssertSuccess(alterTableResult -> {
          Long lastInsertId2 = createTableResult.property(MySQLClient.LAST_INSERTED_ID);
          ctx.assertNull(lastInsertId2);
          conn.query("INSERT INTO last_insert_id(val) VALUES('test')").execute(ctx.asyncAssertSuccess(insertResult1 -> {
            Long lastInsertId3 = insertResult1.property(MySQLClient.LAST_INSERTED_ID);
            ctx.assertEquals(1234L, lastInsertId3);
            conn.query("INSERT INTO last_insert_id(val) VALUES('test2')").execute(ctx.asyncAssertSuccess(insertResult2 -> {
              Long lastInsertId4 = insertResult2.property(MySQLClient.LAST_INSERTED_ID);
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
      .setPreparedStatementCacheMaxSize(1024), ctx.asyncAssertSuccess(conn -> {
      conn.query("SHOW VARIABLES LIKE 'max_prepared_stmt_count'").execute(ctx.asyncAssertSuccess(res1 -> {
        Row row = res1.iterator().next();
        int maxPreparedStatementCount = Integer.parseInt(row.getString(1));
        ctx.assertEquals("max_prepared_stmt_count", row.getString(0));
        ctx.assertEquals(1024, maxPreparedStatementCount);
        for (int i = 0; i < (1024 + 256); i++) {
          String value = "value-" + i;
          for (int j = 0; j < 2; j++) {
            conn.preparedQuery("SELECT '" + value + "'").execute(ctx.asyncAssertSuccess(res2 -> {
              ctx.assertEquals(value, res2.iterator().next().getString(0));
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
        ctx.assertEquals(1024, maxPreparedStatementCount);

        for (int i = 0; i < 2000; i++) {
          conn.preparedQuery("SELECT 'test'").execute(ctx.asyncAssertSuccess(res2 -> {
            ctx.assertEquals("test", res2.iterator().next().getString(0));
          }));
        }
      }));
    }));
  }

  @Test
  public void testCachePreparedStatementBatchWithSameSql(TestContext ctx) {
    MySQLConnection.connect(vertx, options.setCachePreparedStatements(true), ctx.asyncAssertSuccess(conn -> {
      conn.query("SHOW VARIABLES LIKE 'max_prepared_stmt_count'").execute(ctx.asyncAssertSuccess(res1 -> {
        Row row = res1.iterator().next();
        int maxPreparedStatementCount = Integer.parseInt(row.getString(1));
        ctx.assertEquals("max_prepared_stmt_count", row.getString(0));
        ctx.assertEquals(1024, maxPreparedStatementCount);

        for (int i = 0; i < 2000; i++) {
          int val = i * 1000;
          List<Tuple> tuples = new ArrayList<>();
          tuples.add(Tuple.of(val));
          tuples.add(Tuple.of(val + 1));
          conn.preparedQuery("Select cast(? AS CHAR)").executeBatch(tuples, ctx.asyncAssertSuccess(res2 -> {
            String v1 = res2.iterator().next().getString(0);
            String v2 = res2.next().iterator().next().getString(0);
            ctx.assertEquals("" + val, v1);
            ctx.assertEquals("" + (val + 1), v2);
          }));
        }
      }));
    }));
  }

  @Test
  public void testAutoClosingNonCacheOneShotPreparedQueryStatement(TestContext ctx) {
    MySQLConnection.connect(vertx, options.setCachePreparedStatements(false), ctx.asyncAssertSuccess(conn -> {
      conn.query("SHOW VARIABLES LIKE 'max_prepared_stmt_count'").execute(ctx.asyncAssertSuccess(res1 -> {
        Row row = res1.iterator().next();
        int maxPreparedStatementCount = Integer.parseInt(row.getString(1));
        ctx.assertEquals("max_prepared_stmt_count", row.getString(0));
        ctx.assertEquals(1024, maxPreparedStatementCount);

        for (int i = 0; i < 2000; i++) {
          // if we don't close the statement automatically in the codec, the statement handles would leak and raise an statement limit error
          conn.preparedQuery("SELECT 'test'").execute(ctx.asyncAssertSuccess(res2 -> {
            ctx.assertEquals("test", res2.iterator().next().getString(0));
          }));
        }
      }));
    }));
  }

  @Test
  public void testAutoClosingNonCacheOneShotPreparedBatchStatement(TestContext ctx) {
    MySQLConnection.connect(vertx, options.setCachePreparedStatements(false), ctx.asyncAssertSuccess(conn -> {
      conn.query("SHOW VARIABLES LIKE 'max_prepared_stmt_count'").execute(ctx.asyncAssertSuccess(res0 -> {
        Row row = res0.iterator().next();
        int maxPreparedStatementCount = Integer.parseInt(row.getString(1));
        ctx.assertEquals("max_prepared_stmt_count", row.getString(0));
        ctx.assertEquals(1024, maxPreparedStatementCount);

        for (int i = 0; i < 2000; i++) {
          // if we don't close the statement automatically in the codec, the statement handles would leak and raise an statement limit error
          List<Tuple> params = Arrays.asList(Tuple.of(1), Tuple.of(2), Tuple.of(3));
          conn.preparedQuery("SELECT CAST(? AS CHAR)").executeBatch(params, ctx.asyncAssertSuccess(res1 -> {
            ctx.assertEquals("1", res1.iterator().next().getString(0));
            RowSet<Row> res2 = res1.next();
            ctx.assertEquals("2", res2.iterator().next().getString(0));
            RowSet<Row> res3 = res2.next();
            ctx.assertEquals("3", res3.iterator().next().getString(0));
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
    Buffer fileData = Buffer.buffer();
    for (int i = 0; i < 10000; i++) {
      fileData.appendString("Fluffy,Harold,cat,f,1993-02-04,NULL")
        .appendString("\n")
        .appendString("Bowser,Diane,dog,m,1979-08-31,1995-07-29")
        .appendString("\n")
        .appendString("Whistler,Gwen,bird,NULL,1997-12-09,NULL")
        .appendString("\n");
    }
    fileSystem.createTempFile(null, null, ctx.asyncAssertSuccess(filename -> {
      fileSystem.writeFile(filename, fileData, ctx.asyncAssertSuccess(write -> {
        MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
          conn.query("TRUNCATE TABLE localinfile").execute(ctx.asyncAssertSuccess(cleanup -> {
            conn.query("LOAD DATA LOCAL INFILE '" + filename + "' INTO TABLE localinfile FIELDS TERMINATED BY ',' ENCLOSED BY '\"' LINES TERMINATED BY '\\n';").execute(ctx.asyncAssertSuccess(v -> {
              conn.query("SELECT * FROM localinfile").execute(ctx.asyncAssertSuccess(rowSet -> {
                ctx.assertEquals(30000, rowSet.size());
                RowIterator<Row> iterator = rowSet.iterator();
                for (int i = 0; i < 10000; i++) {
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
                }
                conn.close();
              }));
            }));
          }));
        }));
      }));
    }));
  }

  @Test
  public void testLocalInfileRequestEmptyFile(TestContext ctx) {
    FileSystem fileSystem = vertx.fileSystem();
    Buffer fileData = Buffer.buffer();
    fileSystem.createTempFile(null, null, ctx.asyncAssertSuccess(filename -> {
      fileSystem.writeFile(filename, fileData, ctx.asyncAssertSuccess(write -> {
        MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
          conn.query("TRUNCATE TABLE localinfile").execute(ctx.asyncAssertSuccess(cleanup -> {
            conn.query("LOAD DATA LOCAL INFILE '" + filename + "' INTO TABLE localinfile FIELDS TERMINATED BY ',' ENCLOSED BY '\"' LINES TERMINATED BY '\\n';").execute(ctx.asyncAssertSuccess(v -> {
              conn.query("SELECT * FROM localinfile").execute(ctx.asyncAssertSuccess(rowSet -> {
                ctx.assertEquals(0, rowSet.size());
                conn.close();
              }));
            }));
          }));
        }));
      }));
    }));
  }

  @Test
  public void testLocalInfileRequestInPackets(TestContext ctx) {
    FileSystem fileSystem = vertx.fileSystem();
    Buffer fileData = Buffer.buffer();
    for (int i = 0; i < 200000; i++) {
      fileData.appendString("Fluffy,Harold,cat,f,1993-02-04,NULL")
        .appendString("\n")
        .appendString("Bowser,Diane,dog,m,1979-08-31,1995-07-29")
        .appendString("\n")
        .appendString("Whistler,Gwen,bird,NULL,1997-12-09,NULL")
        .appendString("\n");
    }
    ctx.assertTrue(fileData.length() > 0xFFFFFF);
    fileSystem.createTempFile(null, null, ctx.asyncAssertSuccess(filename -> {
      fileSystem.writeFile(filename, fileData, ctx.asyncAssertSuccess(write -> {
        MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
          conn.query("TRUNCATE TABLE localinfile").execute(ctx.asyncAssertSuccess(cleanup -> {
            conn.query("LOAD DATA LOCAL INFILE '" + filename + "' INTO TABLE localinfile FIELDS TERMINATED BY ',' ENCLOSED BY '\"' LINES TERMINATED BY '\\n';").execute(ctx.asyncAssertSuccess(v -> {
              conn.query("SELECT * FROM localinfile").execute(ctx.asyncAssertSuccess(rowSet -> {
                ctx.assertEquals(600000, rowSet.size());
                RowIterator<Row> iterator = rowSet.iterator();
                for (int i = 0; i < 200000; i++) {
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
                }
                conn.close();
              }));
            }));
          }));
        }));
      }));
    }));
  }
}

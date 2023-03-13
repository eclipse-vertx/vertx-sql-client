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
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MySQLStoredProgramsTest extends MySQLTestBase {
  Vertx vertx;
  MySQLConnectOptions options;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = new MySQLConnectOptions(MySQLTestBase.options);
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testMultiStatement(TestContext ctx) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT 1; SELECT \'test\';").execute(ctx.asyncAssertSuccess(result -> {
        Row row1 = result.iterator().next();
        ctx.assertEquals(1, row1.getInteger(0));
        Row row2 = result.next().iterator().next();
        ctx.assertEquals("test", row2.getValue(0));
        ctx.assertEquals("test", row2.getString(0));
        conn.close();
      }));
    }));
  }

  @Test
  public void testMultiResult(TestContext ctx) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("CREATE TEMPORARY TABLE ins ( id INT );").execute(ctx.asyncAssertSuccess(createTable -> {
        conn.query("DROP PROCEDURE IF EXISTS multi;").execute(ctx.asyncAssertSuccess(cleanProcedure -> {
          conn.query("CREATE PROCEDURE multi()\n" +
            "BEGIN\n" +
            "    SELECT 123;\n" +
            "    SELECT 456;\n" +
            "    INSERT INTO ins VALUES (1);\n" +
            "    INSERT INTO ins VALUES (2);\n" +
            "END;").execute(ctx.asyncAssertSuccess(createProcedure -> {
            conn.query("CALL multi();").execute(ctx.asyncAssertSuccess(result -> {
              // example borrowed from https://dev.mysql.com/doc/dev/mysql-server/8.0.12/page_protocol_command_phase_sp.html#sect_protocol_command_phase_sp_multi_resultset
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(123, result.iterator().next().getInteger(0));

              RowSet<Row> secondResult = result.next();
              ctx.assertEquals(1, secondResult.size());
              ctx.assertEquals(456, secondResult.iterator().next().getInteger(0));

              RowSet<Row> thirdResult = secondResult.next();
              ctx.assertEquals(0, thirdResult.size());
              if (rule.isUsingMariaDB()) {
                ctx.assertEquals(2, thirdResult.rowCount());
              } else {
                ctx.assertEquals(1, thirdResult.rowCount()); // will only return the affected_rows of the last INSERT statement
              }

              conn.query("SELECT id FROM ins").execute(ctx.asyncAssertSuccess(queryRes -> {
                ctx.assertEquals(2, queryRes.size());
                RowIterator<Row> rowIterator = queryRes.iterator();
                Row row1 = rowIterator.next();
                ctx.assertEquals(1, row1.getValue(0));
                Row row2 = rowIterator.next();
                ctx.assertEquals(2, row2.getValue(0));
                conn.close();
              }));
            }));
          }));
        }));
      }));
    }));
  }

  @Test
  public void testInParameters(TestContext ctx) {
    // example borrowed from https://dev.mysql.com/doc/refman/8.0/en/stored-programs-defining.html
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("DROP PROCEDURE IF EXISTS dorepeat;").execute(ctx.asyncAssertSuccess(cleanProcedure -> {
        conn.query("CREATE PROCEDURE dorepeat(p1 INT)\n" +
          "BEGIN\n" +
          "    SET @x = 0;\n" +
          "    REPEAT\n" +
          "        SET @x = @x + 1;\n" +
          "    UNTIL @x > p1 END REPEAT;\n" +
          "end;").execute(ctx.asyncAssertSuccess(createProcedure -> {
          conn.query("CALL dorepeat(1000);").execute(ctx.asyncAssertSuccess(callProcedure -> {
            conn.query("SELECT @x;").execute(ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              Row row = result.iterator().next();
              ctx.assertEquals(1001, row.getInteger(0));
              conn.close();
            }));
          }));
        }));
      }));
    }));
  }

  @Test
  public void testOutParameters(TestContext ctx) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("DROP PROCEDURE IF EXISTS test_out_parameter;").execute(ctx.asyncAssertSuccess(cleanProcedure -> {
        conn.query("CREATE PROCEDURE test_out_parameter(OUT p1 VARCHAR(20))\n" +
          "BEGIN\n" +
          "    SELECT 'hello,world!' INTO p1;\n" +
          "end;").execute(ctx.asyncAssertSuccess(createProcedure -> {
          conn.query("CALL test_out_parameter(@OUT);").execute(ctx.asyncAssertSuccess(callProcedure -> {
            conn.query("SELECT @OUT;").execute(ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              Row row = result.iterator().next();
              ctx.assertEquals("hello,world!", row.getValue(0));
              ctx.assertEquals("hello,world!", row.getString(0));
              conn.close();
            }));
          }));
        }));
      }));
    }));
  }

  @Test
  public void testInOutParameters(TestContext ctx) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("DROP PROCEDURE IF EXISTS test_inout_parameter;").execute(ctx.asyncAssertSuccess(cleanProcedure -> {
        conn.query("CREATE PROCEDURE test_inout_parameter(INOUT p1 INT)\n" +
          "BEGIN\n" +
          "    SET p1 = p1 + 12345;\n" +
          "end;").execute(ctx.asyncAssertSuccess(createProcedure -> {
          conn.query("SET @INOUT = 98765;\n" +
            "CALL test_inout_parameter(@INOUT);").execute(ctx.asyncAssertSuccess(callProcedure -> {
            conn.query("SELECT @INOUT;").execute(ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              Row row = result.iterator().next();
              ctx.assertEquals(111110, row.getInteger(0));
              conn.close();
            }));
          }));
        }));
      }));
    }));
  }
}

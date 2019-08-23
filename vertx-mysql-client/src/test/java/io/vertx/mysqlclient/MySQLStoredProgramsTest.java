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
      conn.query("SELECT 1; SELECT \'test\';", ctx.asyncAssertSuccess(result -> {
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
      conn.query("CREATE TEMPORARY TABLE ins ( id INT );", ctx.asyncAssertSuccess(createTable -> {
        conn.query("DROP PROCEDURE IF EXISTS multi;", ctx.asyncAssertSuccess(cleanProcedure -> {
          conn.query("CREATE PROCEDURE multi()\n" +
            "BEGIN\n" +
            "    SELECT 123;\n" +
            "    SELECT 456;\n" +
            "    INSERT INTO ins VALUES (1);\n" +
            "    INSERT INTO ins VALUES (2);\n" +
            "END;", ctx.asyncAssertSuccess(createProcedure -> {
            conn.query("CALL multi();", ctx.asyncAssertSuccess(result -> {
              // example borrowed from https://dev.mysql.com/doc/dev/mysql-server/8.0.12/page_protocol_command_phase_sp.html#sect_protocol_command_phase_sp_multi_resultset
              ctx.assertEquals(1, result.size());
              ctx.assertEquals(123, result.iterator().next().getInteger(0));

              RowSet secondResult = result.next();
              ctx.assertEquals(1, secondResult.size());
              ctx.assertEquals(456, secondResult.iterator().next().getInteger(0));

              RowSet thirdResult = secondResult.next();
              ctx.assertEquals(0, thirdResult.size());
              ctx.assertEquals(1, thirdResult.rowCount()); // will only return the affected_rows of the last INSERT statement

              conn.query("SELECT id FROM ins", ctx.asyncAssertSuccess(queryRes -> {
                ctx.assertEquals(2, queryRes.size());
                RowIterator rowIterator = queryRes.iterator();
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
}

package io.vertx.mysqlclient;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.Tuple;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MySQLCollationTest extends MySQLTestBase {
  private static final String PREPARE_TESTING_TABLE_DATA = "CREATE TEMPORARY TABLE chinese_city (\n" +
    "\tid INTEGER,\n" +
    "\tcity_name VARCHAR(20)) COLLATE utf8_general_ci;\n" +
    "INSERT INTO chinese_city VALUES (1, '\u5317\u4EAC');\n" +
    "INSERT INTO chinese_city VALUES (2, '\u4E0A\u6D77');\n" +
    "INSERT INTO chinese_city VALUES (3, '\u5E7F\u5DDE');\n" +
    "INSERT INTO chinese_city VALUES (4, '\u6DF1\u5733');\n" +
    "INSERT INTO chinese_city VALUES (5, '\u6B66\u6C49');\n" +
    "INSERT INTO chinese_city VALUES (6, '\u6210\u90FD');";
  private static final String PREPARE_TESTING_COLUMN_DATA = "CREATE TEMPORARY TABLE chinese_city (\n" +
    "\tid INTEGER,\n" +
    "\tcity_name VARCHAR(20) COLLATE gbk_chinese_ci);\n" +
    "INSERT INTO chinese_city VALUES (1, '\u5317\u4EAC');\n" +
    "INSERT INTO chinese_city VALUES (2, '\u4E0A\u6D77');\n" +
    "INSERT INTO chinese_city VALUES (3, '\u5E7F\u5DDE');\n" +
    "INSERT INTO chinese_city VALUES (4, '\u6DF1\u5733');\n" +
    "INSERT INTO chinese_city VALUES (5, '\u6B66\u6C49');\n" +
    "INSERT INTO chinese_city VALUES (6, '\u6210\u90FD');";

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
  public void testConnectionCollation(TestContext ctx) {
    MySQLConnectOptions connectOptions = options.setCollation("gbk_chinese_ci");
    MySQLConnection.connect(vertx, connectOptions, ctx.asyncAssertSuccess(conn -> {
      conn.query("SHOW VARIABLES LIKE 'collation_connection';", ctx.asyncAssertSuccess(res -> {
        Row row = res.iterator().next();
        ctx.assertEquals("gbk_chinese_ci", row.getString("Value"));
        conn.close();
      }));
    }));
  }

  @Test
  public void testConnectionCharset(TestContext ctx) {
    MySQLConnectOptions connectOptions = options.setCollation(null).setCharset("gbk");
    MySQLConnection.connect(vertx, connectOptions, ctx.asyncAssertSuccess(conn -> {
      conn.query("SHOW VARIABLES LIKE 'collation_connection';", ctx.asyncAssertSuccess(res -> {
        ctx.assertEquals("gbk_chinese_ci", res.iterator().next().getString("Value"));
        conn.query("SHOW VARIABLES LIKE 'character_set_connection';", ctx.asyncAssertSuccess(res2 -> {
          ctx.assertEquals("gbk", res2.iterator().next().getString("Value"));
          conn.close();
        }));
      }));
    }));
  }

  @Test
  public void testTableCollationBinary(TestContext ctx) {
    testBinary(ctx, PREPARE_TESTING_TABLE_DATA);
  }

  @Test
  public void testTableCollationText(TestContext ctx) {
    testText(ctx, PREPARE_TESTING_TABLE_DATA);
  }

  @Test
  public void testTableCharsetBinary(TestContext ctx) {
    testBinary(ctx, PREPARE_TESTING_TABLE_DATA);
  }

  @Test
  public void testTableCharsetText(TestContext ctx) {
    testText(ctx, PREPARE_TESTING_TABLE_DATA);
  }

  @Test
  public void testColumnCollationBinary(TestContext ctx) {
    testBinary(ctx, PREPARE_TESTING_COLUMN_DATA);
  }

  @Test
  public void testColumnCollationText(TestContext ctx) {
    testText(ctx, PREPARE_TESTING_COLUMN_DATA);
  }

  @Test
  public void testColumnCharsetBinary(TestContext ctx) {
    testBinary(ctx, PREPARE_TESTING_COLUMN_DATA);
  }

  @Test
  public void testColumnCharsetText(TestContext ctx) {
    testText(ctx, PREPARE_TESTING_COLUMN_DATA);
  }

  @Test
  public void testEmoji(TestContext ctx) {
    // use these for tests 😀🤣😊😇😳😱👍🖐⚽
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("CREATE TEMPORARY TABLE emoji(\n" +
        "\tid INTEGER,\n" +
        "\texpression VARCHAR(10)\n" +
        ");\n" +
        "INSERT INTO emoji VALUES (1, '\uD83D\uDE00');\n" +
        "INSERT INTO emoji VALUES (2, '\uD83E\uDD23');\n" +
        "INSERT INTO emoji VALUES (3, '\uD83D\uDE0A');\n" +
        "INSERT INTO emoji VALUES (4, '\uD83D\uDE07');\n" +
        "INSERT INTO emoji VALUES (5, '\uD83D\uDE33');\n" +
        "INSERT INTO emoji VALUES (6, '\uD83D\uDE31');\n" +
        "INSERT INTO emoji VALUES (7, '\uD83D\uDC4D');\n" +
        "INSERT INTO emoji VALUES (8, '\uD83D\uDD90');\n" +
        "INSERT INTO emoji VALUES (9, '\u26bd');", ctx.asyncAssertSuccess(res0 -> {
        conn.query("SELECT id, expression FROM emoji", ctx.asyncAssertSuccess(res1 -> {
          RowIterator iterator = res1.iterator();
          Row row1 = iterator.next();
          ctx.assertEquals(1, row1.getInteger("id"));
          ctx.assertEquals("\uD83D\uDE00", row1.getString("expression"));
          Row row2 = iterator.next();
          ctx.assertEquals(2, row2.getInteger("id"));
          ctx.assertEquals("\uD83E\uDD23", row2.getString("expression"));
          Row row3 = iterator.next();
          ctx.assertEquals(3, row3.getInteger("id"));
          ctx.assertEquals("\uD83D\uDE0A", row3.getString("expression"));
          Row row4 = iterator.next();
          ctx.assertEquals(4, row4.getInteger("id"));
          ctx.assertEquals("\uD83D\uDE07", row4.getString("expression"));
          Row row5 = iterator.next();
          ctx.assertEquals(5, row5.getInteger("id"));
          ctx.assertEquals("\uD83D\uDE33", row5.getString("expression"));
          Row row6 = iterator.next();
          ctx.assertEquals(6, row6.getInteger("id"));
          ctx.assertEquals("\uD83D\uDE31", row6.getString("expression"));
          Row row7 = iterator.next();
          ctx.assertEquals(7, row7.getInteger("id"));
          ctx.assertEquals("\uD83D\uDC4D", row7.getString("expression"));
          Row row8 = iterator.next();
          ctx.assertEquals(8, row8.getInteger("id"));
          ctx.assertEquals("\uD83D\uDD90", row8.getString("expression"));
          Row row9 = iterator.next();
          ctx.assertEquals(9, row9.getInteger("id"));
          ctx.assertEquals("\u26bd", row9.getString("expression"));
          conn.close();
        }));
      }));
    }));
  }

  private void testBinary(TestContext ctx, String prepareDataSql) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query(prepareDataSql, ctx.asyncAssertSuccess(res0 -> {
        conn.preparedQuery("SELECT id, city_name FROM chinese_city where city_name = ?", Tuple.tuple().addString("\u5317\u4EAC"), ctx.asyncAssertSuccess(res1 -> {
          ctx.assertEquals(1, res1.size());
          Row row = res1.iterator().next();
          ctx.assertEquals(1, row.getInteger("id"));
          ctx.assertEquals("\u5317\u4EAC", row.getString("city_name"));
          conn.close();
        }));
      }));
    }));
  }

  private void testText(TestContext ctx, String prepareDataSql) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query(prepareDataSql, ctx.asyncAssertSuccess(res0 -> {
        conn.query("SELECT id, city_name FROM chinese_city", ctx.asyncAssertSuccess(res1 -> {
          ctx.assertEquals(6, res1.size());
          RowIterator iterator = res1.iterator();
          Row row1 = iterator.next();
          ctx.assertEquals(1, row1.getInteger("id"));
          ctx.assertEquals("\u5317\u4EAC", row1.getString("city_name"));
          Row row2 = iterator.next();
          ctx.assertEquals(2, row2.getInteger("id"));
          ctx.assertEquals("\u4E0A\u6D77", row2.getString("city_name"));
          Row row3 = iterator.next();
          ctx.assertEquals(3, row3.getInteger("id"));
          ctx.assertEquals("\u5E7F\u5DDE", row3.getString("city_name"));
          Row row4 = iterator.next();
          ctx.assertEquals(4, row4.getInteger("id"));
          ctx.assertEquals("\u6DF1\u5733", row4.getString("city_name"));
          Row row5 = iterator.next();
          ctx.assertEquals(5, row5.getInteger("id"));
          ctx.assertEquals("\u6B66\u6C49", row5.getString("city_name"));
          Row row6 = iterator.next();
          ctx.assertEquals(6, row6.getInteger("id"));
          ctx.assertEquals("\u6210\u90FD", row6.getString("city_name"));
          conn.close();
        }));
      }));
    }));
  }
}

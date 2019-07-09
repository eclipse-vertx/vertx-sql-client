package io.vertx.mysqlclient;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * We need to decide which part of these test to be migrated into TCK.
 * TODO shall we have collector tests in TCK? collector is more a feature for upper application rather than driver SPI feature
 */
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
  public void testColumnCollation(TestContext ctx) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("CREATE TEMPORARY TABLE chinese_city (\n" +
        "\tid INTEGER,\n" +
        "\tcity_name VARCHAR(20) COLLATE gbk_chinese_ci);\n" +
        "INSERT INTO chinese_city VALUES (1, '北京');\n" +
        "INSERT INTO chinese_city VALUES (2, '上海');\n" +
        "INSERT INTO chinese_city VALUES (3, '广州');\n" +
        "INSERT INTO chinese_city VALUES (4, '深圳');\n" +
        "INSERT INTO chinese_city VALUES (5, '武汉');\n" +
        "INSERT INTO chinese_city VALUES (6, '成都');", ctx.asyncAssertSuccess(res0 -> {
        conn.query("SELECT id, city_name FROM chinese_city", ctx.asyncAssertSuccess(res1 -> {
          ctx.assertEquals(6, res1.size());
          RowIterator iterator = res1.iterator();
          Row row1 = iterator.next();
          ctx.assertEquals(1, row1.getInteger("id"));
          ctx.assertEquals("北京", row1.getString("city_name"));
          Row row2 = iterator.next();
          ctx.assertEquals(2, row2.getInteger("id"));
          ctx.assertEquals("上海", row2.getString("city_name"));
          Row row3 = iterator.next();
          ctx.assertEquals(3, row3.getInteger("id"));
          ctx.assertEquals("广州", row3.getString("city_name"));
          Row row4 = iterator.next();
          ctx.assertEquals(4, row4.getInteger("id"));
          ctx.assertEquals("深圳", row4.getString("city_name"));
          Row row5 = iterator.next();
          ctx.assertEquals(5, row5.getInteger("id"));
          ctx.assertEquals("武汉", row5.getString("city_name"));
          Row row6 = iterator.next();
          ctx.assertEquals(6, row6.getInteger("id"));
          ctx.assertEquals("成都", row6.getString("city_name"));
        }));
      }));
    }));
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
        }));
      }));
    }));
  }

  @Test
  public void testCachePreparedStatementWithDifferentSql(TestContext ctx) {
    // we set the cache size to be the same with max_prepared_stmt_count
    MySQLConnection.connect(vertx, options.setCachePreparedStatements(true)
      .setPreparedStatementCacheMaxSize(16382), ctx.asyncAssertSuccess(conn -> {
      conn.query("SHOW VARIABLES LIKE 'max_prepared_stmt_count'", ctx.asyncAssertSuccess(res1 -> {
        Row row = res1.iterator().next();
        int maxPreparedStatementCount = Integer.parseInt(row.getString(1));
        ctx.assertEquals("max_prepared_stmt_count", row.getString(0));
        ctx.assertEquals(16382, maxPreparedStatementCount);

        for (int i = 0; i < 10000; i++) {
          String randomString = UUID.randomUUID().toString();
          for (int j = 0; j < 2; j++) {
            conn.preparedQuery("SELECT '" + randomString + "'", ctx.asyncAssertSuccess(res2 -> {
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
      conn.query("SHOW VARIABLES LIKE 'max_prepared_stmt_count'", ctx.asyncAssertSuccess(res1 -> {
        Row row = res1.iterator().next();
        int maxPreparedStatementCount = Integer.parseInt(row.getString(1));
        ctx.assertEquals("max_prepared_stmt_count", row.getString(0));
        ctx.assertEquals(16382, maxPreparedStatementCount);

        for (int i = 0; i < 20000; i++) {
          conn.preparedQuery("SELECT 'test'", ctx.asyncAssertSuccess(res2 -> {
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
      conn.query("SELECT REPEAT('abcde', 4000000)", ctx.asyncAssertSuccess(rowSet -> {
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
      conn.preparedQuery("UPDATE datatype SET `LongBlob` = ? WHERE id = 2", Tuple.of(buffer), ctx.asyncAssertSuccess(v -> {
        conn.preparedQuery("SELECT id, `LongBlob` FROM datatype WHERE id = 2", ctx.asyncAssertSuccess(rowSet -> {
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
  public void testMultiResult(TestContext ctx) {
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
  public void testSimpleQueryCollector(TestContext ctx) {
    Collector<Row, ?, Map<Integer, DummyObject>> collector = Collectors.toMap(
      row -> row.getInteger("id"),
      row -> new DummyObject(row.getInteger("id"),
        row.getShort("Int2"),
        row.getInteger("Int3"),
        row.getInteger("Int4"),
        row.getLong("Int8"),
        row.getFloat("Float"),
        row.getDouble("Double"),
        row.getString("Varchar"))
    );

    DummyObject expected = new DummyObject(1, (short) 32767, 8388607, 2147483647, 9223372036854775807L,
      123.456f, 1.234567d, "HELLO,WORLD");

    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT * FROM collectorTest WHERE id = 1", collector, ctx.asyncAssertSuccess(result -> {
        Map<Integer, DummyObject> map = result.value();
        DummyObject actual = map.get(1);
        ctx.assertEquals(expected, actual);
        conn.close();
      }));
    }));
  }

  @Test
  public void testPreparedCollector(TestContext ctx) {
    Collector<Row, ?, Map<Integer, DummyObject>> collector = Collectors.toMap(
      row -> row.getInteger("id"),
      row -> new DummyObject(row.getInteger("id"),
        row.getShort("Int2"),
        row.getInteger("Int3"),
        row.getInteger("Int4"),
        row.getLong("Int8"),
        row.getFloat("Float"),
        row.getDouble("Double"),
        row.getString("Varchar"))
    );

    DummyObject expected = new DummyObject(1, (short) 32767, 8388607, 2147483647, 9223372036854775807L,
      123.456f, 1.234567d, "HELLO,WORLD");

    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT * FROM collectorTest WHERE id = ?", Tuple.of(1), collector, ctx.asyncAssertSuccess(result -> {
        Map<Integer, DummyObject> map = result.value();
        DummyObject actual = map.get(1);
        ctx.assertEquals(expected, actual);
        conn.close();
      }));
    }));
  }

  // this class is for verifying the use of Collector API
  private static class DummyObject {
    private int id;
    private short int2;
    private int int3;
    private int int4;
    private long int8;
    private float floatNum;
    private double doubleNum;
    private String varchar;

    public DummyObject(int id, short int2, int int3, int int4, long int8, float floatNum, double doubleNum, String varchar) {
      this.id = id;
      this.int2 = int2;
      this.int3 = int3;
      this.int4 = int4;
      this.int8 = int8;
      this.floatNum = floatNum;
      this.doubleNum = doubleNum;
      this.varchar = varchar;
    }

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    public short getInt2() {
      return int2;
    }

    public void setInt2(short int2) {
      this.int2 = int2;
    }

    public int getInt3() {
      return int3;
    }

    public void setInt3(int int3) {
      this.int3 = int3;
    }

    public int getInt4() {
      return int4;
    }

    public void setInt4(int int4) {
      this.int4 = int4;
    }

    public long getInt8() {
      return int8;
    }

    public void setInt8(long int8) {
      this.int8 = int8;
    }

    public float getFloatNum() {
      return floatNum;
    }

    public void setFloatNum(float floatNum) {
      this.floatNum = floatNum;
    }

    public double getDoubleNum() {
      return doubleNum;
    }

    public void setDoubleNum(double doubleNum) {
      this.doubleNum = doubleNum;
    }

    public String getVarchar() {
      return varchar;
    }

    public void setVarchar(String varchar) {
      this.varchar = varchar;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      DummyObject that = (DummyObject) o;

      if (id != that.id) return false;
      if (int2 != that.int2) return false;
      if (int3 != that.int3) return false;
      if (int4 != that.int4) return false;
      if (int8 != that.int8) return false;
      if (Float.compare(that.floatNum, floatNum) != 0) return false;
      if (Double.compare(that.doubleNum, doubleNum) != 0) return false;
      return varchar != null ? varchar.equals(that.varchar) : that.varchar == null;
    }

    @Override
    public int hashCode() {
      int result;
      long temp;
      result = id;
      result = 31 * result + (int) int2;
      result = 31 * result + int3;
      result = 31 * result + int4;
      result = 31 * result + (int) (int8 ^ (int8 >>> 32));
      result = 31 * result + (floatNum != +0.0f ? Float.floatToIntBits(floatNum) : 0);
      temp = Double.doubleToLongBits(doubleNum);
      result = 31 * result + (int) (temp ^ (temp >>> 32));
      result = 31 * result + (varchar != null ? varchar.hashCode() : 0);
      return result;
    }

    @Override
    public String toString() {
      return "DummyObject{" +
        "id=" + id +
        ", int2=" + int2 +
        ", int3=" + int3 +
        ", int4=" + int4 +
        ", int8=" + int8 +
        ", floatNum=" + floatNum +
        ", doubleNum=" + doubleNum +
        ", varchar='" + varchar + '\'' +
        '}';
    }
  }
}

package io.vertx.myclient;

import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @deprecated Migrate all these tests into TCK
 * TODO shall we have collector tests in TCK? collector is more a feature for upper application rather than driver SPI feature
 */
@RunWith(VertxUnitRunner.class)
@Deprecated
public class MysqlQueryTest extends MysqlTestBase {

  Vertx vertx;
  PgConnectOptions options;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = new PgConnectOptions(MysqlTestBase.options);
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
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

    MyClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT * FROM collectorTest WHERE id = 1", collector, ctx.asyncAssertSuccess(result -> {
        Map<Integer, DummyObject> map = result.value();
        DummyObject actual = map.get(1);
        ctx.assertEquals(expected, actual);
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

    MyClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT * FROM collectorTest WHERE id = ?", Tuple.of(1), collector, ctx.asyncAssertSuccess(result -> {
        Map<Integer, DummyObject> map = result.value();
        DummyObject actual = map.get(1);
        ctx.assertEquals(expected, actual);
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

package io.vertx.pgclient.data;

import io.vertx.ext.unit.TestContext;
import io.vertx.pgclient.PgConnection;
import io.vertx.sqlclient.Query;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;
import org.junit.Test;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.function.BiFunction;

public class InetCodecTest extends DataTypeTestBase {

  @Test
  public void testBinaryDecodeINET(TestContext ctx) throws Exception {
    testDecodeINET(ctx, SqlClient::preparedQuery);
  }

  @Test
  public void testTextDecodeINET(TestContext ctx) throws Exception {
      testDecodeINET(ctx, SqlClient::query);
  }

  private void testDecodeINET(TestContext ctx, BiFunction<SqlClient, String, Query<RowSet<Row>>> a) throws Exception {
    InetAddress addr1 = Inet4Address.getByName("0.1.2.3");
    InetAddress addr2 = Inet6Address.getByName("2001:0db8:0a0b:12f0:0000:0000:0000:0001");
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      a.apply(conn, "SELECT " +
        "'0.1.2.3'::INET," +
        "'0.1.2.3/4'::INET," +
        "'2001:0db8:0a0b:12f0:0000:0000:0000:0001'::INET," +
        "'2001:0db8:0a0b:12f0:0000:0000:0000:0001/4'::INET").execute(ctx.asyncAssertSuccess(rows -> {
        ctx.assertEquals(1, rows.size());
        Row row = rows.iterator().next();
        Inet v1 = (Inet) row.getValue(0);
        Inet v2 = (Inet) row.getValue(1);
        Inet v3 = (Inet) row.getValue(2);
        Inet v4 = (Inet) row.getValue(3);
        ctx.assertEquals(addr1, v1.getAddress());
        ctx.assertNull(v1.getNetmask());
        ctx.assertEquals(addr1, v2.getAddress());
        ctx.assertEquals(4, v2.getNetmask());
        ctx.assertEquals(addr2, v3.getAddress());
        ctx.assertNull(v3.getNetmask());
        ctx.assertEquals(addr2, v4.getAddress());
        ctx.assertEquals(4, v4.getNetmask());
      }));
    }));
  }

  @Test
  public void testBinaryEncodeINET(TestContext ctx) throws Exception {
    InetAddress addr1 = Inet4Address.getByName("0.1.2.3");
    InetAddress addr2 = Inet6Address.getByName("2001:0db8:0a0b:12f0:0000:0000:0000:0001");
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT ($1::INET)::VARCHAR, ($2::INET)::VARCHAR, ($3::INET)::VARCHAR, ($4::INET)::VARCHAR").execute(Tuple.of(
        new Inet().setAddress(addr1),
        new Inet().setAddress(addr1).setNetmask(4),
        new Inet().setAddress(addr2),
        new Inet().setAddress(addr2).setNetmask(4)
        ),
        ctx.asyncAssertSuccess(rows -> {
          ctx.assertEquals(1, rows.size());
          Row row = rows.iterator().next();
          String v1 = row.getString(0);
          String v2 = row.getString(1);
          String v3 = row.getString(2);
          String v4 = row.getString(3);
          ctx.assertEquals("0.1.2.3/32", v1);
          ctx.assertEquals("0.1.2.3/4", v2);
          ctx.assertEquals("2001:db8:a0b:12f0::1/128", v3);
          ctx.assertEquals("2001:db8:a0b:12f0::1/4", v4);
        }));
    }));
  }
}

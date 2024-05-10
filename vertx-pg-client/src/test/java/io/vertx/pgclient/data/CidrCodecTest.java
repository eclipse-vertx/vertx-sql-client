package io.vertx.pgclient.data;

import io.vertx.ext.unit.TestContext;
import io.vertx.pgclient.PgConnection;
import io.vertx.sqlclient.*;
import org.junit.Test;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class CidrCodecTest extends DataTypeTestBase{

  @Test
  public void testValidIPv4() throws Exception {
    InetAddress address = InetAddress.getByName("192.168.1.1");
    Cidr cidr = new Cidr();
    cidr.setAddress(address);
    cidr.setNetmask(24);
    assertEquals(address, cidr.getAddress());
    assertEquals(Integer.valueOf(24), cidr.getNetmask());
  }

  @Test
  public void testValidIPv6() throws Exception {
    InetAddress address = InetAddress.getByName("fe80::f03c:91ff:feae:e944");
    Cidr cidr = new Cidr();
    cidr.setAddress(address);
    cidr.setNetmask(64);
    assertEquals(address, cidr.getAddress());
    assertEquals(Integer.valueOf(64), cidr.getNetmask());
  }

  @Test
  public void testInvalidNetmaskIPv4() throws Exception {
    InetAddress address = InetAddress.getByName("192.168.1.1");
    Cidr cidr = new Cidr();
    cidr.setAddress(address);
    assertThrows(IllegalArgumentException.class, () -> cidr.setNetmask(33));
  }

  @Test
  public void testInvalidNetmaskIPv6() throws Exception {
    InetAddress address = InetAddress.getByName("fe80::f03c:91ff:feae:e944");
    Cidr cidr = new Cidr();
    cidr.setAddress(address);
    assertThrows(IllegalArgumentException.class, () -> cidr.setNetmask(129));
  }

  @Test
  public void testBinaryDecodeCIDR(TestContext ctx) throws Exception {
    testDecodeCIDR(ctx, SqlClient::preparedQuery);
  }

  private void testDecodeCIDR(TestContext ctx, BiFunction<SqlClient, String, Query<RowSet<Row>>> a) throws Exception {
    InetAddress addr1 = Inet4Address.getByName("128.0.0.0");
    InetAddress addr2 = Inet6Address.getByName("2001:0db8:1234:0000:0000:0000:0000:0000");
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      a.apply(conn, "SELECT " +
          "'128.0.0.0'::CIDR," +
          "'128.0.0.0/4'::CIDR," +
          "'2001:0db8:1234:0000:0000:0000:0000:0000'::CIDR," +
          "'2001:0db8:1234:0000:0000:0000:0000:0000/56'::CIDR")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(rows -> {
          ctx.assertEquals(1, rows.size());
          Row row = rows.iterator().next();
          Cidr v1 = (Cidr) row.getValue(0);
          Cidr v2 = (Cidr) row.getValue(1);
          Cidr v3 = (Cidr) row.getValue(2);
          Cidr v4 = (Cidr) row.getValue(3);
          ctx.assertEquals(addr1, v1.getAddress());
          ctx.assertEquals(32,v1.getNetmask());
          ctx.assertEquals(addr1, v2.getAddress());
          ctx.assertEquals(4, v2.getNetmask());
          ctx.assertEquals(addr2, v3.getAddress());
          ctx.assertEquals(128, v3.getNetmask());
          ctx.assertEquals(addr2, v4.getAddress());
          ctx.assertEquals(56, v4.getNetmask());
        }));
    }));
  }

  @Test
  public void testBinaryEncodeCIDR(TestContext ctx) throws Exception {
    InetAddress addr1 = Inet4Address.getByName("128.0.0.0");
    InetAddress addr2 = Inet6Address.getByName("2001:0db8:1234:0000:0000:0000:0000:0000");
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery("SELECT ($1::CIDR)::VARCHAR, ($2::CIDR)::VARCHAR, ($3::CIDR)::VARCHAR, ($4::CIDR)::VARCHAR")
        .execute(Tuple.of(
          new Cidr().setAddress(addr1),
          new Cidr().setAddress(addr1).setNetmask(4),
          new Cidr().setAddress(addr2),
          new Cidr().setAddress(addr2).setNetmask(56)
        ))
        .onComplete(ctx.asyncAssertSuccess(rows -> {
          ctx.assertEquals(1, rows.size());
          Row row = rows.iterator().next();
          String v1 = row.getString(0);
          String v2 = row.getString(1);
          String v3 = row.getString(2);
          String v4 = row.getString(3);
          ctx.assertEquals("128.0.0.0/32", v1);
          ctx.assertEquals("128.0.0.0/4", v2);
          ctx.assertEquals("2001:db8:1234::/128", v3);
          ctx.assertEquals("2001:db8:1234::/56", v4);
        }));
    }));
  }

}

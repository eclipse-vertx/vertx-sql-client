package io.vertx.mysqlclient;

import io.vertx.core.Vertx;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mysqlclient.junit.MySQLRule;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MySQLTLSTest {

  @ClassRule
  public static MySQLRule rule = MySQLRule.SHARED_TLS_INSTANCE;

  Vertx vertx;
  MySQLConnectOptions options;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = new MySQLConnectOptions(rule.options());

    /*
     * For testing we have to drop using the TLSv1.2.
     *
     * MySQL 5.x uses yaSSL by default which does not support TLSv1.2,
     * and TLSv1.2 is only supported by the OpenSSL-based MySQL server.
     * see https://mysqlserverteam.com/ssltls-improvements-in-mysql-5-7-10/
     * and https://dev.mysql.com/doc/refman/5.7/en/encrypted-connection-protocols-ciphers.html for more details.
     */
    options.removeEnabledSecureTransportProtocol("TLSv1.2");
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

//  @Test
//  @Ignore("We do not configure to enforce TLS on the client side for the testing")
//  public void testFailWithTlsDisabled(TestContext ctx) {
//    options.setSslMode(SslMode.DISABLED);
//    MySQLConnection.connect(vertx, options, ctx.asyncAssertFailure(error -> {
//      // TLS support is forced to be enabled on the client side
//    }));
//  }

  @Test
  public void testSuccessWithDisabledSslMode(TestContext ctx) {
    options.setSslMode(SslMode.DISABLED);
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      ctx.assertFalse(conn.isSSL());
      conn.query("SELECT 1", ctx.asyncAssertSuccess(res -> {
        ctx.assertEquals(1, res.size());
        conn.close();
      }));
    }));
  }

  @Test
  public void testSuccessWithPreferredSslMode(TestContext ctx) {
    options.setSslMode(SslMode.PREFERRED);
    options.setPemTrustOptions(new PemTrustOptions().addCertPath("tls/files/ca.pem"));
    options.setPemKeyCertOptions(new PemKeyCertOptions()
      .setCertPath("tls/files/client-cert.pem")
      .setKeyPath("tls/files/client-key.pem"));

    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      ctx.assertTrue(conn.isSSL());
      conn.query("SELECT 1", ctx.asyncAssertSuccess(res -> {
        ctx.assertEquals(1, res.size());
        conn.close();
      }));
    }));
  }

  @Test
  public void testSuccessWithRequiredSslMode(TestContext ctx) {
    options.setSslMode(SslMode.REQUIRED);
    options.setPemTrustOptions(new PemTrustOptions().addCertPath("tls/files/ca.pem"));
    options.setPemKeyCertOptions(new PemKeyCertOptions()
      .setCertPath("tls/files/client-cert.pem")
      .setKeyPath("tls/files/client-key.pem"));

    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      ctx.assertTrue(conn.isSSL());
      conn.query("SELECT 1", ctx.asyncAssertSuccess(res -> {
        ctx.assertEquals(1, res.size());
        conn.close();
      }));
    }));
  }

  @Test
  public void testSuccessWithOnlyCertificate(TestContext ctx) {
    options.setSslMode(SslMode.REQUIRED);
    options.setPemTrustOptions(new PemTrustOptions().addCertPath("tls/files/ca.pem"));

    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      ctx.assertTrue(conn.isSSL());
      conn.query("SELECT 1", ctx.asyncAssertSuccess(res -> {
        ctx.assertEquals(1, res.size());
        conn.close();
      }));
    }));
  }

  @Test
  public void testSuccessWithoutCertificate(TestContext ctx) {
    options.setSslMode(SslMode.REQUIRED);
    options.setTrustAll(true);

    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      ctx.assertTrue(conn.isSSL());
      conn.query("SELECT 1", ctx.asyncAssertSuccess(res -> {
        ctx.assertEquals(1, res.size());
        conn.close();
      }));
    }));
  }

  @Test
  public void testSuccessWithVerifyCaSslMode(TestContext ctx) {
    options.setSslMode(SslMode.VERIFY_CA);
    options.setPemTrustOptions(new PemTrustOptions().addCertPath("tls/files/ca.pem"));
    options.setPemKeyCertOptions(new PemKeyCertOptions()
      .setCertPath("tls/files/client-cert.pem")
      .setKeyPath("tls/files/client-key.pem"));

    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      ctx.assertTrue(conn.isSSL());
      conn.query("SELECT 1", ctx.asyncAssertSuccess(res -> {
        ctx.assertEquals(1, res.size());
        conn.close();
      }));
    }));
  }

  @Test
  public void testConnFailWithVerifyCaSslMode(TestContext ctx) {
    options.setSslMode(SslMode.VERIFY_CA);
    options.setTrustAll(true);
    options.setPemKeyCertOptions(new PemKeyCertOptions()
      .setCertPath("tls/files/client-cert.pem")
      .setKeyPath("tls/files/client-key.pem"));

    MySQLConnection.connect(vertx, options, ctx.asyncAssertFailure(error -> {
      ctx.assertEquals("Trust options must be specified under VERIFY_CA ssl-mode.", error.getMessage());
    }));
  }

  @Test
  public void testPoolFailWithVerifyCaSslMode(TestContext ctx) {
    options.setSslMode(SslMode.VERIFY_CA);
    options.setTrustAll(true);
    options.setPemKeyCertOptions(new PemKeyCertOptions()
      .setCertPath("tls/files/client-cert.pem")
      .setKeyPath("tls/files/client-key.pem"));

    try {
      MySQLPool.pool(vertx, options, new PoolOptions());
    } catch (IllegalArgumentException e) {
      ctx.assertEquals("Trust options must be specified under VERIFY_CA ssl-mode.", e.getMessage());
    }
  }

  @Test
  public void testConnFailWithVerifyIdentitySslMode(TestContext ctx) {
    options.setSslMode(SslMode.VERIFY_IDENTITY);
    options.setPemTrustOptions(new PemTrustOptions().addCertPath("tls/files/ca.pem"));
    options.setPemKeyCertOptions(new PemKeyCertOptions()
      .setCertPath("tls/files/client-cert.pem")
      .setKeyPath("tls/files/client-key.pem"));

    MySQLConnection.connect(vertx, options, ctx.asyncAssertFailure(error -> {
      ctx.assertEquals("Host verification algorithm must be specified under VERIFY_IDENTITY ssl-mode.", error.getMessage());
    }));
  }

  @Test
  public void testConnFail(TestContext ctx) {
    options.setSslMode(SslMode.REQUIRED);

    MySQLConnection.connect(vertx, options, ctx.asyncAssertFailure(error -> {
    }));
  }

  @Test
  public void testChangeUser(TestContext ctx) {
    options.setSslMode(SslMode.REQUIRED);
    options.setPemTrustOptions(new PemTrustOptions().addCertPath("tls/files/ca.pem"));

    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT current_user()", ctx.asyncAssertSuccess(res1 -> {
        Row row1 = res1.iterator().next();
        String username = row1.getString(0);
        ctx.assertEquals("mysql", username.substring(0, username.lastIndexOf('@')));
        MySQLConnectOptions changeUserOptions = new MySQLConnectOptions()
          .setUser("superuser")
          .setPassword("password")
          .setDatabase("emptyschema");
        conn.changeUser(changeUserOptions, ctx.asyncAssertSuccess(v2 -> {
          conn.query("SELECT current_user();SELECT database();", ctx.asyncAssertSuccess(res2 -> {
            ctx.assertEquals("superuser@%", res2.iterator().next().getString(0));
            ctx.assertEquals("emptyschema", res2.next().iterator().next().getValue(0));
            conn.close();
          }));
        }));
      }));
    }));
  }
}

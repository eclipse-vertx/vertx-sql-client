package io.vertx.sqlclient.tck;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import org.junit.Test;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.spi.Driver;

public abstract class DriverTestBase {

  protected abstract SqlConnectOptions defaultOptions();

  @Test
  public void testServiceLoader(TestContext ctx) {
    List<Driver> drivers = new ArrayList<>();
    for (Driver d : ServiceLoader.load(Driver.class)) {
      drivers.add(d);
    }
    assertEquals("Expected to find exactly 1 Driver but found: " + drivers, 1, drivers.size());
  }

  @Test
  public void testAcceptsOptions(TestContext ctx) {
    assertTrue(getDriver().acceptsOptions(defaultOptions()));
  }

  @Test
  public void testAcceptsGenericOptions(TestContext ctx) {
    assertTrue(getDriver().acceptsOptions(new SqlConnectOptions()));
  }

  @Test
  public void testRejectsOtherOptions(TestContext ctx) {
    assertFalse(getDriver().acceptsOptions(new BogusOptions()));
  }

  @Test
  public void testCreatePoolFromDriver01(TestContext ctx) {
    Pool p = getDriver().createPool(null, Collections.singletonList(defaultOptions()), new PoolOptions());
    p.getConnection(ctx.asyncAssertSuccess(ar -> {
      ar.close();
    }));
  }

  @Test
  public void testCreatePoolFromDriver02(TestContext ctx) {
    Pool p = getDriver().createPool(null, Collections.singletonList(new SqlConnectOptions(defaultOptions())), new PoolOptions());
    p.getConnection(ctx.asyncAssertSuccess(ar -> {
      ar.close();
    }));
  }

  @Test
  public void testCreatePoolFromDriver03(TestContext ctx) {
    Pool p = getDriver().createPool(null, Collections.singletonList(defaultOptions()), new PoolOptions().setMaxSize(1));
    p.getConnection(ctx.asyncAssertSuccess(ar -> {
      ar.close();
    }));
  }

  @Test
  public void testCreatePoolFromDriver04(TestContext ctx) {
    Pool p = getDriver().createPool(Vertx.vertx(), Collections.singletonList(defaultOptions()), new PoolOptions().setMaxSize(1));
    p.getConnection(ctx.asyncAssertSuccess(ar -> {
      ar.close();
    }));
  }

  @Test
  public void testCreatePool01(TestContext ctx) {
    Pool.pool(defaultOptions()).getConnection(ctx.asyncAssertSuccess(ar -> {
      ar.close();
    }));
  }

  @Test
  public void testCreatePool02(TestContext ctx) {
    Pool.pool(new SqlConnectOptions(defaultOptions()), new PoolOptions()).getConnection(ctx.asyncAssertSuccess(ar -> {
      ar.close();
    }));
  }

  @Test
  public void testCreatePool03(TestContext ctx) {
    Pool.pool(defaultOptions(), new PoolOptions().setMaxSize(1)).getConnection(ctx.asyncAssertSuccess(ar -> {
      ar.close();
    }));
  }

  @Test
  public void testCreatePool04(TestContext ctx) {
    Pool.pool(Vertx.vertx(), defaultOptions(), new PoolOptions()).getConnection(ctx.asyncAssertSuccess(ar -> {
      ar.close();
    }));
  }

  @Test
  public void testCreatePool05(TestContext ctx) {
    // The default options will be an instanceof the driver-specific class, so manually copy
    // each option over onto a fresh generic options object to force the generic constructor path
    SqlConnectOptions defaults = defaultOptions();
    SqlConnectOptions opts = new SqlConnectOptions()
          .setHost(defaults.getHost())
          .setPort(defaults.getPort())
          .setDatabase(defaults.getDatabase())
          .setUser(defaults.getUser())
          .setPassword(defaults.getPassword());
    Pool.pool(opts).getConnection(ctx.asyncAssertSuccess(ar -> {
      ar.close();
    }));
  }

  @Test(expected = ServiceConfigurationError.class)
  public void testRejectCreatePool01(TestContext ctx) {
    Pool.pool(new BogusOptions());
  }

  @Test(expected = ServiceConfigurationError.class)
  public void testRejectCreatePool02(TestContext ctx) {
    Pool.pool(new BogusOptions(), new PoolOptions());
  }

  @Test(expected = ServiceConfigurationError.class)
  public void testRejectCreatePool03(TestContext ctx) {
    Pool.pool(Vertx.vertx(), new BogusOptions(), new PoolOptions());
  }

  public static class BogusOptions extends SqlConnectOptions {
  }

  private Driver getDriver() {
    return ServiceLoader.load(Driver.class).iterator().next();
  }
}

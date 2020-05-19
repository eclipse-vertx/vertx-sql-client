package io.vertx.sqlclient.tck;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
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
import io.vertx.sqlclient.spi.Driver.KnownDrivers;

public abstract class DriverTestBase {
  
  protected abstract SqlConnectOptions defaultOptions();
  
  protected abstract String getDriverName();
  
  @Test
  public void testServiceLoader(TestContext ctx) {
    List<Driver> drivers = new ArrayList<>();
    for (Driver d : ServiceLoader.load(Driver.class)) {
      drivers.add(d);
    }
    assertEquals("Expected to find exactly 1 Driver but found: " + drivers, 1, drivers.size());
  }

  @Test
  public void testDriverName(TestContext ctx) {
    Driver d = getDriver();
    ctx.assertNotNull(d.name());
    ctx.assertEquals(getDriverName(), d.name());
    try {
      Driver.KnownDrivers.valueOf(d.name());
    } catch (IllegalArgumentException e) {
      ctx.fail("Driver returned a name that is not a known value: " + d.name());
    }
  }
  
  @Test
  public void testCreateOptions(TestContext ctx) {
    SqlConnectOptions opts = Driver.createConnectOptions(getDriverName());
    ctx.assertNotNull(opts);
    ctx.assertNotNull(opts.getHost());
    ctx.assertTrue(opts.getPort() > 1024, "Default connect options should have a valid port by default");
    
    KnownDrivers d = KnownDrivers.valueOf(getDriverName());
    SqlConnectOptions opts2 = Driver.createConnectOptions(d);
    ctx.assertNotNull(opts2);
    ctx.assertEquals(opts, opts2, "opts=" + opts.toJson().encode() + "  opts2=" + opts2.toJson().encode());
  }
  
  @Test
  public void testCreatePoolWithCreatedOptions(TestContext ctx) {
    SqlConnectOptions defaultOpts = defaultOptions();
    SqlConnectOptions newOpts = Driver.createConnectOptions(getDriverName())
        .setHost(defaultOpts.getHost())
        .setPort(defaultOpts.getPort())
        .setDatabase(defaultOpts.getDatabase())
        .setUser(defaultOpts.getUser())
        .setPassword(defaultOpts.getPassword());
    Pool pool = Pool.pool(newOpts);
    pool.getConnection(ctx.asyncAssertSuccess(ar -> {
      ar.close();
    }));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testRejectsOtherOptions(TestContext ctx) {
    getDriver().createPool(new BogusOptions());
  }
  
  @Test
  public void testCreatePoolFromDriver01(TestContext ctx) {
    Pool p = getDriver().createPool(defaultOptions());
    p.getConnection(ctx.asyncAssertSuccess(ar -> {
      ar.close();
    }));
  }
  
  @Test
  public void testCreatePoolFromDriver02(TestContext ctx) {
    Pool p = getDriver().createPool(defaultOptions(), new PoolOptions().setMaxSize(1));
    p.getConnection(ctx.asyncAssertSuccess(ar -> {
      ar.close();
    }));
  }
  
  @Test
  public void testCreatePoolFromDriver03(TestContext ctx) {
    Pool p = getDriver().createPool(Vertx.vertx(), defaultOptions(), new PoolOptions().setMaxSize(1));
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
    Pool.pool(defaultOptions(), new PoolOptions().setMaxSize(1)).getConnection(ctx.asyncAssertSuccess(ar -> {
      ar.close();
    }));
  }
  
  @Test
  public void testCreatePool03(TestContext ctx) {
    Pool.pool(Vertx.vertx(), defaultOptions(), new PoolOptions()).getConnection(ctx.asyncAssertSuccess(ar -> {
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

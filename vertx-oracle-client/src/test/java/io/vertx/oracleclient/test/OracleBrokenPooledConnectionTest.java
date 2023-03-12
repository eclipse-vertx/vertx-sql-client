/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.oracleclient.test;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.oracleclient.OraclePool;
import io.vertx.oracleclient.test.junit.OracleRule;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.ProxyServer;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicReference;

@RunWith(VertxUnitRunner.class)
public class OracleBrokenPooledConnectionTest extends OracleTestBase {

  @ClassRule
  public static OracleRule oracle = OracleRule.SHARED_INSTANCE;

  OracleConnectOptions options = oracle.options();
  OraclePool pool;

  @After
  public void tearDown(TestContext ctx) throws Exception {
    if (pool != null) {
      pool.close().onComplete(ctx.asyncAssertSuccess());
    }
  }

  @Test
  public void testBrokenConnectionEvicted(TestContext ctx) {
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    AtomicReference<ProxyServer.Connection> proxyConn = new AtomicReference<>();
    proxy.proxyHandler(conn -> {
      proxyConn.set(conn);
      conn.connect();
    });
    pool = OraclePool.pool(vertx, new OracleConnectOptions(options).setPort(8080), new PoolOptions().setMaxSize(1));
    proxy.listen(8080, options.getHost(), ctx.asyncAssertSuccess(listen -> {
      pool.query("SELECT 1 FROM DUAL").execute(ctx.asyncAssertSuccess(executed -> {
        ProxyServer.Connection proxyConn1 = proxyConn.get();
        ctx.assertNotNull(proxyConn1);
        Async async = ctx.async();
        proxyConn1.clientCloseHandler(onClose1 -> {
          pool.query("SELECT 1 FROM DUAL").execute(ctx.asyncAssertFailure(ignored -> {
            pool.query("SELECT 1 FROM DUAL").execute(ar -> {
              if (ar.succeeded()) {
                async.complete();
              } else {
                ctx.fail(ar.cause());
              }
            });
          }));
        });
        proxyConn1.close();
      }));
    }));
  }
}

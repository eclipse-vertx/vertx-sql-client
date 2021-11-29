/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.oracleclient.test;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.oracleclient.OraclePool;
import io.vertx.oracleclient.test.junit.OracleRule;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class OracleGeneratedKeysTest extends OracleTestBase {

  @ClassRule
  public static OracleRule oracle = OracleRule.SHARED_INSTANCE;

  OraclePool pool;

  @Before
  public void setUp() throws Exception {
    pool = OraclePool.pool(vertx, oracle.options(), new PoolOptions());
  }

  @Test
  public void shouldRetrieveGeneratedKey(TestContext ctx) {
    pool.withConnection(conn -> {
      return conn.query(" DROP TABLE EntityWithIdentity").execute()
        .otherwiseEmpty()
        .compose(v -> conn.query("CREATE TABLE EntityWithIdentity\n" +
          "(\n" +
          "    id       NUMBER(19, 0) GENERATED AS IDENTITY,\n" +
          "    name     VARCHAR2(255 CHAR),\n" +
          "    position NUMBER(10, 0),\n" +
          "    PRIMARY KEY (id)\n" +
          ")").execute())
        .compose(v -> conn.preparedQuery("INSERT INTO EntityWithIdentity (name, position)\n" +
          "VALUES (?, ?)").execute(Tuple.of("john", 3)));
    }, ctx.asyncAssertSuccess(rows -> {
      Row generatedKeys = rows.property(OraclePool.GENERATED_KEYS);
      ctx.assertNotNull(generatedKeys);
      System.out.println("generatedKeys = " + generatedKeys.toJson().encodePrettily());
    }));
  }

  @After
  public void tearDown(TestContext ctx) throws Exception {
    pool.close(ctx.asyncAssertSuccess());
  }
}

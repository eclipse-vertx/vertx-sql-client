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
package io.vertx.oracle.test;

import io.vertx.oracle.OracleConnectOptions;
import io.vertx.oracle.OracleConnection;
import io.vertx.oracle.OraclePool;
import io.vertx.oracle.test.junit.OracleRule;
import io.vertx.sqlclient.*;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

public class OraclePoolTest extends OracleTestBase {

  @Rule
  public OracleRule oracle;

  static final String DROP_TABLE = "DROP TABLE fruits";
  static final String CREATE_TABLE = "CREATE TABLE fruits (" +
    "id integer PRIMARY KEY, " +
    "name VARCHAR(100), " +
    "quantity INTEGER)";
  static final String INSERT = "INSERT INTO fruits (id, name, quantity) VALUES (?, ?, ?)";

  @Test
  public void test() {
    OraclePool pool = OraclePool.pool(vertx, new OracleConnectOptions()
        .setHost(OracleRule.getDatabaseHost())
        .setPort(OracleRule.getDatabasePort())
        .setUser(OracleRule.getUser())
        .setPassword(OracleRule.getPassword())
        .setDatabase(OracleRule.getDatabase()),
      new PoolOptions().setMaxSize(1)
    );

    SqlConnection connection = await(pool.getConnection());
    System.out.println(connection);

    System.out.println(
      "metadata: " + connection.databaseMetadata().fullVersion() + " " + connection.databaseMetadata()
        .productName());

    try {
      await(connection.query(DROP_TABLE).execute());
    } catch (Exception ignored) {

    }

    await(connection.query(CREATE_TABLE).execute());

    await(connection.prepare(INSERT)
      .flatMap(ps -> ps.query().execute(Tuple.of(1, "apple", 10))));
    await(connection.prepare(INSERT)
      .flatMap(ps -> ps.query().execute(Tuple.of(2, "pear", 5))));
    await(connection.prepare(INSERT)
      .flatMap(ps -> ps.query().execute(Tuple.of(3, "mango", 3))));

    RowSet<Row> rows = await(connection.query("SELECT * FROM fruits").execute());
    rows.forEach(row -> System.out.printf("[%d] %s : %d%n", row.get(Integer.class, 0), row.get(String.class, 1),
      row.get(Integer.class, 2)));

    RowSet<Row> res = await(connection.query("SELECT * FROM fruits WHERE id = 1").execute());
    System.out.println("Select one : " + res.iterator().next().get(String.class, 1));

    // Batch
    System.out.println("Batch insert:");
    RowSet<Row> set = await(connection.preparedQuery(INSERT)
      .executeBatch(List.of(
        Tuple.of(4, "pineapple", 1),
        Tuple.of(5, "kiwi", 2),
        Tuple.of(6, "orange", 3),
        Tuple.of(7, "strawberry", 20)
      )));

    System.out.println(set.size());

    rows = await(connection.query("SELECT * FROM fruits").execute());
    rows.forEach(row -> System.out.printf("[%d] %s : %d%n", row.get(Integer.class, 0), row.get(String.class, 1),
      row.get(Integer.class, 2)));

    System.out.println("Transaction:");
    await(connection.begin()
      .flatMap(tx ->
        connection.prepare(INSERT).flatMap(ps -> ps.query().execute(Tuple.of(20, "olive", 200)))
          .flatMap(x -> tx.commit())
          .eventually(x -> tx.rollback())
      ));

    await(connection.begin()
      .flatMap(tx ->
        connection.prepare(INSERT).flatMap(ps -> ps.query().execute(Tuple.of(23, "nope", -2)))
          .flatMap(x -> tx.rollback())
          .eventually(x -> tx.rollback())
      ));

    rows = await(connection.query("SELECT * FROM fruits").execute());
    rows.forEach(row -> System.out.printf("[%d] %s : %d%n", row.get(Integer.class, 0), row.get(String.class, 1),
      row.get(Integer.class, 2)));

    System.out.println("Ping");
    System.out.println(await(((OracleConnection) connection).ping()));

    await(connection.close());
  }

}

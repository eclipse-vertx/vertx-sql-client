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

package io.vertx.sqlclient.spi;

import io.vertx.core.*;
import io.vertx.sqlclient.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static io.vertx.sqlclient.ServerType.*;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

public class ConnectionFactoryTest {
  Context context = Vertx.vertx().getOrCreateContext();

  static class DummySqlConnection implements SqlConnection {

    @Override
    public Query<RowSet<Row>> query(String sql) {
      return null;
    }

    @Override
    public PreparedQuery<RowSet<Row>> preparedQuery(String sql) {
      return null;
    }

    @Override
    public PreparedQuery<RowSet<Row>> preparedQuery(String sql, PrepareOptions options) {
      return null;
    }

    @Override
    public Future<Void> close() {
      return Future.succeededFuture();
    }

    @Override
    public SqlConnection prepare(String sql, Handler<AsyncResult<PreparedStatement>> handler) {
      return null;
    }

    @Override
    public Future<PreparedStatement> prepare(String sql) {
      return null;
    }

    @Override
    public SqlConnection prepare(String sql, PrepareOptions options, Handler<AsyncResult<PreparedStatement>> handler) {
      return null;
    }

    @Override
    public Future<PreparedStatement> prepare(String sql, PrepareOptions options) {
      return null;
    }

    @Override
    public SqlConnection exceptionHandler(Handler<Throwable> handler) {
      return null;
    }

    @Override
    public SqlConnection closeHandler(Handler<Void> handler) {
      return null;
    }

    @Override
    public void begin(Handler<AsyncResult<Transaction>> handler) {

    }

    @Override
    public Future<Transaction> begin() {
      return null;
    }

    @Override
    public boolean isSSL() {
      return false;
    }

    @Override
    public void close(Handler<AsyncResult<Void>> handler) {

    }

    @Override
    public DatabaseMetadata databaseMetadata() {
      return null;
    }
  }

  static class StaticFactory implements ConnectionFactory {

    public StaticFactory(ServerType serverType, SqlConnection connection) {
      this(serverType, null, connection, false);
    }

    public StaticFactory(ServerType serverType, SqlConnection connection, boolean failing) {
      this(serverType, null, connection, failing);
    }

    public StaticFactory(ServerType initialServerType, ServerType promotedServerType, SqlConnection connection) {
      this(initialServerType, promotedServerType, connection, false);
    }

    public StaticFactory(ServerType initialServerType, ServerType promotedServerType, SqlConnection connection, boolean failing) {
      this.serverType = initialServerType;
      this.promotedServerType = promotedServerType;
      this.connection = connection;
      this.failing = failing;
    }

    private ServerType serverType;
    private ServerType promotedServerType;
    private final SqlConnection connection;
    private final boolean failing;

    @Override
    public ServerType getServerType() {
      return serverType;
    }

    @Override
    public Future<SqlConnection> connect(Context context) {
      if (promotedServerType != null) {
        serverType = promotedServerType;
      }
      if (failing) {
        return Future.failedFuture("unable to connect");
      } else {
        return Future.succeededFuture(connection);
      }
    }

    @Override
    public void close(Promise<Void> promise) {}
  }

  @Test
  public void testRoundRobinSelector() {
    SqlConnection conn1 = new DummySqlConnection();
    SqlConnection conn2 = new DummySqlConnection();
    SqlConnection conn3 = new DummySqlConnection();
    ConnectionFactory factory = ConnectionFactory.roundRobinSelector(
      Stream.of(conn1, conn2, conn3).map(conn -> new StaticFactory(UNDEFINED, conn)).collect(toList())
    );
    assertEquals(conn1, factory.connect(context).result());
    assertEquals(conn2, factory.connect(context).result());
    assertEquals(conn3, factory.connect(context).result());
    assertEquals(conn1, factory.connect(context).result());
  }

  @Test
  public void testAnySelector() {
    SqlConnection conn1 = new DummySqlConnection();
    SqlConnection conn2 = new DummySqlConnection();
    SqlConnection conn3 = new DummySqlConnection();
    ConnectionFactory factory = ConnectionFactory.selector(
      Stream.of(conn1, conn2, conn3).map(conn -> new StaticFactory(UNDEFINED, conn)).collect(toList()),
      ServerRequirement.ANY
    );
    assertEquals(conn1, factory.connect(context).result());
    assertEquals(conn2, factory.connect(context).result());
    assertEquals(conn3, factory.connect(context).result());
    assertEquals(conn1, factory.connect(context).result());
  }

  @Test
  public void testPrimarySelectorAmongUndefinedFactories() {
    SqlConnection conn1 = new DummySqlConnection();
    SqlConnection conn2 = new DummySqlConnection();
    List<ConnectionFactory> factories = new ArrayList<>();
    Stream.of(conn1, conn2).map(conn -> new StaticFactory(UNDEFINED, conn)).collect(toCollection(() -> factories));
    SqlConnection primaryConn = new DummySqlConnection();
    factories.add(new StaticFactory(PRIMARY, primaryConn));
    ConnectionFactory factory = ConnectionFactory.selector(factories, ServerRequirement.PRIMARY);
    assertEquals(primaryConn, factory.connect(context).result());
    assertEquals(primaryConn, factory.connect(context).result());
    assertEquals(primaryConn, factory.connect(context).result());
    assertEquals(primaryConn, factory.connect(context).result());
  }

  @Test
  public void testPrimarySelectorAmongReplicaFactories() {
    SqlConnection conn1 = new DummySqlConnection();
    SqlConnection conn2 = new DummySqlConnection();
    List<ConnectionFactory> factories = new ArrayList<>();
    Stream.of(conn1, conn2).map(conn -> new StaticFactory(REPLICA, conn)).collect(toCollection(() -> factories));
    SqlConnection primaryConn = new DummySqlConnection();
    factories.add(new StaticFactory(PRIMARY, primaryConn));
    ConnectionFactory factory = ConnectionFactory.selector(factories, ServerRequirement.PRIMARY);
    assertEquals(primaryConn, factory.connect(context).result());
    assertEquals(primaryConn, factory.connect(context).result());
    assertEquals(primaryConn, factory.connect(context).result());
    assertEquals(primaryConn, factory.connect(context).result());
  }

  @Test
  public void testPrimarySelectorAmongReplicaAndUndefinedFactories() {
    SqlConnection conn1Replica = new DummySqlConnection();
    SqlConnection conn2Replica = new DummySqlConnection();
    List<ConnectionFactory> factories = new ArrayList<>();
    Stream.of(conn1Replica, conn2Replica).map(conn -> new StaticFactory(REPLICA, conn)).collect(toCollection(() -> factories));
    SqlConnection conn1Undefined = new DummySqlConnection();
    SqlConnection conn2Undefined = new DummySqlConnection();
    Stream.of(conn1Undefined, conn2Undefined).map(conn -> new StaticFactory(UNDEFINED, conn)).collect(toCollection(() -> factories));
    SqlConnection primaryConn = new DummySqlConnection();
    factories.add(new StaticFactory(PRIMARY, primaryConn));
    ConnectionFactory factory = ConnectionFactory.selector(factories, ServerRequirement.PRIMARY);
    assertEquals(primaryConn, factory.connect(context).result());
    assertEquals(primaryConn, factory.connect(context).result());
    assertEquals(primaryConn, factory.connect(context).result());
    assertEquals(primaryConn, factory.connect(context).result());
    assertEquals(primaryConn, factory.connect(context).result());
    assertEquals(primaryConn, factory.connect(context).result());
  }

  @Test
  public void testPrimarySelectorAmongUndefinedFactoriesWithOnePromotedToPrimary() {
    SqlConnection conn1 = new DummySqlConnection();
    SqlConnection conn2 = new DummySqlConnection();
    List<ConnectionFactory> factories = new ArrayList<>();
    Stream.of(conn1, conn2).map(conn -> new StaticFactory(UNDEFINED, conn)).collect(toCollection(() -> factories));
    SqlConnection primaryConn = new DummySqlConnection();
    factories.add(new StaticFactory(UNDEFINED, PRIMARY, primaryConn));
    ConnectionFactory factory = ConnectionFactory.selector(factories, ServerRequirement.PRIMARY);
    assertEquals(primaryConn, factory.connect(context).result());
    assertEquals(primaryConn, factory.connect(context).result());
    assertEquals(primaryConn, factory.connect(context).result());
    assertEquals(primaryConn, factory.connect(context).result());
  }

  @Test
  public void testReplicaSelectorAmongUndefinedFactoriesWithOnePromotedToReplica() {
    SqlConnection conn1 = new DummySqlConnection();
    SqlConnection conn2 = new DummySqlConnection();
    List<ConnectionFactory> factories = new ArrayList<>();
    Stream.of(conn1, conn2).map(conn -> new StaticFactory(UNDEFINED, conn)).collect(toCollection(() -> factories));
    SqlConnection replicaConn = new DummySqlConnection();
    factories.add(new StaticFactory(UNDEFINED, REPLICA, replicaConn));
    ConnectionFactory factory = ConnectionFactory.selector(factories, ServerRequirement.REPLICA);
    assertEquals(replicaConn, factory.connect(context).result());
    assertEquals(replicaConn, factory.connect(context).result());
    assertEquals(replicaConn, factory.connect(context).result());
    assertEquals(replicaConn, factory.connect(context).result());
  }

  @Test
  public void testReplicaSelectorAmongUndefinedFactories() {
    List<ConnectionFactory> factories = new ArrayList<>();
    SqlConnection conn1Undefined = new DummySqlConnection();
    SqlConnection conn2Undefined = new DummySqlConnection();
    Stream.of(conn1Undefined, conn2Undefined).map(conn -> new StaticFactory(UNDEFINED, conn)).collect(toCollection(() -> factories));
    SqlConnection replicaConn1 = new DummySqlConnection();
    SqlConnection replicaConn2 = new DummySqlConnection();
    factories.add(new StaticFactory(REPLICA, replicaConn1));
    factories.add(new StaticFactory(REPLICA, replicaConn2));
    ConnectionFactory factory = ConnectionFactory.selector(factories, ServerRequirement.REPLICA);
    // This is a pathological case for round-robin selector with filtering
    // Due to multiple fallbacks first server with target type will be chosen more often
    // Selector algorithm should be optimized.
    assertEquals(replicaConn1, factory.connect(context).result());
    assertEquals(replicaConn1, factory.connect(context).result());
    assertEquals(replicaConn1, factory.connect(context).result());
    assertEquals(replicaConn2, factory.connect(context).result());
    assertEquals(replicaConn1, factory.connect(context).result());
    assertEquals(replicaConn1, factory.connect(context).result());
    assertEquals(replicaConn1, factory.connect(context).result());
    assertEquals(replicaConn2, factory.connect(context).result());
  }

  @Test
  public void testPreferReplicaSelectorAmongUndefinedFactories() {
    SqlConnection conn1 = new DummySqlConnection();
    SqlConnection conn2 = new DummySqlConnection();
    List<ConnectionFactory> factories = new ArrayList<>();
    Stream.of(conn1, conn2).map(conn -> new StaticFactory(UNDEFINED, PRIMARY, conn)).collect(toCollection(() -> factories));
    SqlConnection replicaConn1 = new DummySqlConnection();
    factories.add(new StaticFactory(UNDEFINED, REPLICA, replicaConn1));
    SqlConnection replicaConn2 = new DummySqlConnection();
    factories.add(new StaticFactory(UNDEFINED, REPLICA, replicaConn2));
    ConnectionFactory factory = ConnectionFactory.selector(factories, ServerRequirement.PREFER_REPLICA);
    // first full round of probing of unknown hosts
    assertEquals(conn1, factory.connect(context).result());
    assertEquals(conn2, factory.connect(context).result());
    assertEquals(replicaConn1, factory.connect(context).result());
    assertEquals(replicaConn2, factory.connect(context).result());
    // then round of prioritizing
    assertEquals(conn1, factory.connect(context).result());
    assertEquals(conn2, factory.connect(context).result());
    assertEquals(replicaConn1, factory.connect(context).result());
    assertEquals(replicaConn2, factory.connect(context).result());
    // then always return prioritized connections
    assertEquals(replicaConn1, factory.connect(context).result());
    assertEquals(replicaConn2, factory.connect(context).result());
    assertEquals(replicaConn1, factory.connect(context).result());
    assertEquals(replicaConn2, factory.connect(context).result());
  }

  @Test
  public void testPreferReplicaSelectorAmongPrimaryAndUndefinedFactoriesWhenReplicaFails() {
    List<ConnectionFactory> factories = new ArrayList<>();
    SqlConnection replicaConn1 = new DummySqlConnection();
    factories.add(new StaticFactory(UNDEFINED, REPLICA, replicaConn1, true));
    SqlConnection replicaConn2 = new DummySqlConnection();
    factories.add(new StaticFactory(UNDEFINED, REPLICA, replicaConn2, true));
    SqlConnection primaryConn = new DummySqlConnection();
    factories.add(new StaticFactory(UNDEFINED, PRIMARY, primaryConn, false));
    SqlConnection primaryConnFailing = new DummySqlConnection();
    factories.add(new StaticFactory(UNDEFINED, PRIMARY, primaryConnFailing, true));
    ConnectionFactory factory = ConnectionFactory.selector(factories, ServerRequirement.PREFER_REPLICA);
    assertEquals(primaryConn, factory.connect(context).result());
    assertEquals(primaryConn, factory.connect(context).result());
    assertEquals(primaryConn, factory.connect(context).result());
    assertEquals(primaryConn, factory.connect(context).result());
  }
}

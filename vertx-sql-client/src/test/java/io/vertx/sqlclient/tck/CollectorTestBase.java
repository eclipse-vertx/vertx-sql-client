/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.sqlclient.tck;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public abstract class CollectorTestBase {
  protected Vertx vertx;
  protected Connector<SqlConnection> connector;

  protected abstract void initConnector();

  protected void connect(Handler<AsyncResult<SqlConnection>> handler) {
    connector.connect(handler);
  }

  @Before
  public void setUp() throws Exception {
    vertx = Vertx.vertx();
    initConnector();
  }

  @After
  public void tearDown(TestContext ctx) {
    connector.close();
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testSimpleQuery(TestContext ctx) {
    Collector<Row, ?, Map<Integer, TestingCollectorObject>> collector = Collectors.toMap(
      row -> row.getInteger("id"),
      row -> new TestingCollectorObject(row.getInteger("id"),
        row.getShort("test_int_2"),
        row.getInteger("test_int_4"),
        row.getLong("test_int_8"),
        row.getFloat("test_float"),
        row.getDouble("test_double"),
        row.getString("test_varchar"))
    );

    TestingCollectorObject expected = new TestingCollectorObject(1, (short) 32767, 2147483647, 9223372036854775807L,
      123.456f, 1.234567d, "HELLO,WORLD");

    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn.createQuery("SELECT * FROM collector_test WHERE id = 1")
        .collecting(collector)
        .execute(ctx.asyncAssertSuccess(result -> {
        Map<Integer, TestingCollectorObject> map = result.value();
        TestingCollectorObject actual = map.get(1);
        ctx.assertEquals(expected, actual);
        conn.close();
      }));
    }));
  }

  @Test
  public void testPreparedQuery(TestContext ctx) {
    Collector<Row, ?, Map<Integer, TestingCollectorObject>> collector = Collectors.toMap(
      row -> row.getInteger("id"),
      row -> new TestingCollectorObject(row.getInteger("id"),
        row.getShort("test_int_2"),
        row.getInteger("test_int_4"),
        row.getLong("test_int_8"),
        row.getFloat("test_float"),
        row.getDouble("test_double"),
        row.getString("test_varchar"))
    );

    TestingCollectorObject expected = new TestingCollectorObject(1, (short) 32767, 2147483647, 9223372036854775807L,
      123.456f, 1.234567d, "HELLO,WORLD");

    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn.createPreparedQuery("SELECT * FROM collector_test WHERE id = 1")
        .collecting(collector)
        .execute(ctx.asyncAssertSuccess(result -> {
        Map<Integer, TestingCollectorObject> map = result.value();
        TestingCollectorObject actual = map.get(1);
        ctx.assertEquals(expected, actual);
        conn.close();
      }));
    }));
  }

  @Test
  public void testCollectorFailureProvidingSupplier(TestContext ctx) {
    RuntimeException cause = new RuntimeException();
    testCollectorFailure(ctx, cause, new CollectorBase() {
      @Override
      public Supplier<Object> supplier() {
        throw cause;
      }
    });
  }

  @Test
  public void testCollectorFailureInSupplier(TestContext ctx) {
    RuntimeException cause = new RuntimeException();
    testCollectorFailure(ctx, cause, new CollectorBase() {
      @Override
      public Supplier<Object> supplier() {
        return () -> {
          throw cause;
        };
      }
    });
  }

  @Test
  public void testCollectorFailureProvidingAccumulator(TestContext ctx) {
    RuntimeException cause = new RuntimeException();
    testCollectorFailure(ctx, cause, new CollectorBase() {
      @Override
      public BiConsumer<Object, Row> accumulator() {
        throw cause;
      }
    });
  }

  @Test
  public void testCollectorFailureInAccumulator(TestContext ctx) {
    RuntimeException cause = new RuntimeException();
    testCollectorFailure(ctx, cause, new CollectorBase() {
      @Override
      public BiConsumer<Object, Row> accumulator() {
        return (o, row) -> {
          throw cause;
        };
      }
    });
  }

  @Test
  public void testCollectorFailureProvidingFinisher(TestContext ctx) {
    RuntimeException cause = new RuntimeException();
    testCollectorFailure(ctx, cause, new CollectorBase() {
      @Override
      public Function<Object, Object> finisher() {
        throw cause;
      }
    });
  }

  @Test
  public void testCollectorFailureInFinisher(TestContext ctx) {
    RuntimeException cause = new RuntimeException();
    testCollectorFailure(ctx, cause, new CollectorBase() {
      @Override
      public Function<Object, Object> finisher() {
        return o -> {
          throw cause;
        };
      }
    });
  }

  private void testCollectorFailure(TestContext ctx, Throwable cause, Collector<Row, Object, Object> collector) {
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn.createQuery("SELECT * FROM collector_test WHERE id = 1")
        .collecting(collector)
        .execute(ctx.asyncAssertFailure(result -> {
        ctx.assertEquals(cause, result);
        conn.close();
      }));
    }));
  }

  // this class is for verifying the use of Collector API
  private static class TestingCollectorObject {
    public int id;
    public short int2;
    public int int4;
    public long int8;
    public float floatNum;
    public double doubleNum;
    public String varchar;

    private TestingCollectorObject(int id, short int2, int int4, long int8, float floatNum, double doubleNum, String varchar) {
      this.id = id;
      this.int2 = int2;
      this.int4 = int4;
      this.int8 = int8;
      this.floatNum = floatNum;
      this.doubleNum = doubleNum;
      this.varchar = varchar;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      TestingCollectorObject that = (TestingCollectorObject) o;

      if (id != that.id) return false;
      if (int2 != that.int2) return false;
      if (int4 != that.int4) return false;
      if (int8 != that.int8) return false;
      if (Float.compare(that.floatNum, floatNum) != 0) return false;
      if (Double.compare(that.doubleNum, doubleNum) != 0) return false;
      return varchar != null ? varchar.equals(that.varchar) : that.varchar == null;
    }
  }

  private static class CollectorBase implements Collector<Row, Object, Object> {
    @Override
    public Supplier<Object> supplier() {
      return () -> null;
    }

    @Override
    public BiConsumer<Object, Row> accumulator() {
      return (a, t) -> {

      };
    }

    @Override
    public BinaryOperator<Object> combiner() {
      return (a, a2) -> null;
    }

    @Override
    public Function<Object, Object> finisher() {
      return a -> null;
    }

    @Override
    public Set<Characteristics> characteristics() {
      return Collections.emptySet();
    }
  }
}

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
package io.vertx.oracleclient.test.tck;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.oracleclient.test.junit.OracleRule;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.tck.CollectorTestBase;
import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@RunWith(VertxUnitRunner.class)
public class OracleCollectorTest extends CollectorTestBase {
  @ClassRule
  public static OracleRule rule = OracleRule.SHARED_INSTANCE;

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }

  @Test
  public void testSimpleQuery(TestContext ctx) {
    Async async = ctx.async();
    Collector<Row, ?, Map<Integer, TestingCollectorObject>> collector = Collectors.toMap((row) -> row.getInteger(0),
      row -> new TestingCollectorObject(row.getInteger(0), row.getShort(1), row.getInteger(2), row.getLong(3),
        row.getFloat(4), row.getDouble(5), row.getString(6)));

    TestingCollectorObject expected = new TestingCollectorObject(1, (short) 32767, 2147483647, 9223372036854775807L,
      123.456F, 1.234567D, "HELLO,WORLD");
    this.connector.connect(ctx.asyncAssertSuccess((conn) -> {
      conn.query("SELECT * FROM test_collector WHERE id = 1").collecting(collector)
        .execute(ctx.asyncAssertSuccess((result) -> {
          Map<Integer, TestingCollectorObject> map = result.value();
          TestingCollectorObject actual = map.get(1);
          ctx.assertEquals(expected, actual);
          conn.close();
          async.complete();
        }));
    }));
  }

  @Test
  public void testPreparedQuery(TestContext ctx) {
    Async async = ctx.async();
    Collector<Row, ?, Map<Integer, TestingCollectorObject>> collector = Collectors.toMap(
      row -> row.getInteger("id"),
      row -> new TestingCollectorObject(row.getInteger(0),
        row.getShort(1),
        row.getInteger(2),
        row.getLong(3),
        row.getFloat(4),
        row.getDouble(5),
        row.getString(6))
    );

    TestingCollectorObject expected = new TestingCollectorObject(1, (short) 32767, 2147483647, 9223372036854775807L,
      123.456f, 1.234567d, "HELLO,WORLD");

    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT * FROM test_collector WHERE id = 1")
        .collecting(collector)
        .execute(ctx.asyncAssertSuccess(result -> {
          Map<Integer, TestingCollectorObject> map = result.value();
          TestingCollectorObject actual = map.get(1);
          ctx.assertEquals(expected, actual);
          conn.close();
          async.complete();
        }));
    }));
  }

  @Test
  public void testCollectorFailureProvidingSupplier(TestContext ctx) {
    RuntimeException cause = new RuntimeException();
    testCollectorFailure(ctx.async(), ctx, cause, new CollectorBase() {
      @Override
      public Supplier<Object> supplier() {
        throw cause;
      }
    });
  }

  @Test
  public void testCollectorFailureInSupplier(TestContext ctx) {
    RuntimeException cause = new RuntimeException();
    testCollectorFailure(ctx.async(), ctx, cause, new CollectorBase() {
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
    testCollectorFailure(ctx.async(), ctx, cause, new CollectorBase() {
      @Override
      public BiConsumer<Object, Row> accumulator() {
        throw cause;
      }
    });
  }

  @Test
  public void testCollectorFailureInAccumulator(TestContext ctx) {
    RuntimeException cause = new RuntimeException();
    testCollectorFailure(ctx.async(), ctx, cause, new CollectorBase() {
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
    testCollectorFailure(ctx.async(), ctx, cause, new CollectorBase() {
      @Override
      public Function<Object, Object> finisher() {
        throw cause;
      }
    });
  }

  @Test
  public void testCollectorFailureInFinisher(TestContext ctx) {
    RuntimeException cause = new RuntimeException();
    testCollectorFailure(ctx.async(), ctx, cause, new CollectorBase() {
      @Override
      public Function<Object, Object> finisher() {
        return o -> {
          throw cause;
        };
      }
    });
  }

  private void testCollectorFailure(Async async, TestContext ctx, Throwable cause,
    Collector<Row, Object, Object> collector) {
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT * FROM test_collector WHERE id = 1")
        .collecting(collector)
        .execute(ctx.asyncAssertFailure(result -> {
          ctx.assertEquals(cause, result);
          conn.close();
          async.complete();
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

    private TestingCollectorObject(int id, short int2, int int4, long int8, float floatNum, double doubleNum,
      String varchar) {
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
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      TestingCollectorObject that = (TestingCollectorObject) o;

      if (id != that.id) {
        return false;
      }
      if (int2 != that.int2) {
        return false;
      }
      if (int4 != that.int4) {
        return false;
      }
      if (int8 != that.int8) {
        return false;
      }
      if (Float.compare(that.floatNum, floatNum) != 0) {
        return false;
      }
      if (Double.compare(that.doubleNum, doubleNum) != 0) {
        return false;
      }
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

  @Override
  public void testCollectorRecycle(TestContext ctx) {
    Assume.assumeTrue(false);
  }

  @Override
  public void testCollectorNoRecycle(TestContext ctx) {
    Assume.assumeTrue(false);
  }
}

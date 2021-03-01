package io.vertx.clickhousenativeclient;

import io.vertx.clickhouse.clickhousenative.ClickhouseNativeConnectOptions;
import io.vertx.clickhouse.clickhousenative.ClickhouseNativeConnection;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.columns.ClickhouseColumns;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlClient;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(VertxUnitRunner.class)
public class BasicClickhouseTest {
  private static final Logger LOG = LoggerFactory.getLogger(BasicClickhouseTest.class);

  @ClassRule
  public static ClickhouseResource rule = new ClickhouseResource();

  private ClickhouseNativeConnectOptions options;
  private Vertx vertx;

  @Before
  public void setup() {
    options = rule.options();
    vertx = Vertx.vertx();
  }

  @After
  public void teardDown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void baseConnectTest(TestContext ctx) {
    ClickhouseNativeConnection.connect(vertx, options, ctx.asyncAssertSuccess(SqlClient::close));
  }

  @Test
  public void loginFailureTest(TestContext ctx) {
    ClickhouseNativeConnectOptions opts = new ClickhouseNativeConnectOptions(options);
    opts.setPassword("wrong-password");
    ClickhouseNativeConnection.connect(vertx, opts, ctx.asyncAssertFailure());
  }

  @Test
  public void testIntegerRanges(TestContext ctx) {
    List<ClickhouseNativeColumnDescriptor> types = Stream.of("Int8", "Int16", "Int32", "Int64")
      .flatMap(el -> Stream.of("Nullable(" + el + ")", "Nullable(U" + el + ")"))
      .map(nm -> ClickhouseColumns.columnDescriptorForSpec(nm, "fake_name"))
      .collect(Collectors.toList());
    List<String> typeNames = types.stream()
      .map(ClickhouseNativeColumnDescriptor::getUnparsedNativeType).collect(Collectors.toList());
    LOG.info("integer columns: " + typeNames);
    Iterator<ClickhouseNativeColumnDescriptor> typesIter = types.iterator();
    ClickhouseNativeConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      scheduleQuery(ctx, typesIter, conn);
    }));
  }

  private void scheduleQuery(TestContext ctx, Iterator<ClickhouseNativeColumnDescriptor> typeIterator, ClickhouseNativeConnection conn) {
    if (typeIterator.hasNext()) {
      ClickhouseNativeColumnDescriptor descr = typeIterator.next();
      String nativeType = descr.getUnparsedNativeType();
      String query = String.format("SELECT CAST(%s, '%s') as min_val, CAST(%s, '%s') as max_val",
        descr.getMinValue(), nativeType, descr.getMaxValue(), nativeType);
      LOG.info("running query: " + query);
      conn.query(query).execute(
        ctx.asyncAssertSuccess(res -> {
          ctx.assertEquals(1, res.size());
          Row row = res.iterator().next();
          ctx.assertEquals(descr.getMinValue(), row.getBigDecimal("min_val").toBigIntegerExact(), nativeType + " type min failure");
          ctx.assertEquals(descr.getMaxValue(), row.getBigDecimal("max_val").toBigIntegerExact(), nativeType + " type max failure");
          scheduleQuery(ctx, typeIterator, conn);
        })
      );
    } else {
      conn.close();
    }
  }

  @Test
  public void baseQueryTest(TestContext ctx) {
    ClickhouseNativeConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("select 4 as resource, 'aa' as str_col1, CAST('abcdef', 'FixedString(6)') as str_col2").execute(
        ctx.asyncAssertSuccess(res1 -> {
          ctx.assertEquals(1, res1.size());
          conn.close();
        })
      );
    }));
  }
}

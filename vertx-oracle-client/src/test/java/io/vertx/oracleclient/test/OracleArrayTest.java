package io.vertx.oracleclient.test;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.oracleclient.OracleBuilder;
import io.vertx.oracleclient.OracleConnection;
import io.vertx.oracleclient.test.junit.OracleRule;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Tuple;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(VertxUnitRunner.class)
public class OracleArrayTest extends OracleTestBase {

  @ClassRule
  public static OracleRule oracle = OracleRule.SHARED_INSTANCE;

  Pool pool;

  @Before
  public void setUp() throws Exception {
    pool = OracleBuilder.pool( builder -> builder
      .connectingTo(oracle.options())
      .using(vertx));
  }

  @Test
  public void testStringArray(TestContext ctx) {
    String[] elements = {"str1", "str2", "str3"};
    pool.withConnection(conn -> {
      Object stringsArray = ((OracleConnection)conn).createArray( "StringArrayType", elements );
      String insertSql = "INSERT INTO StringsArrayTable( id, stringsArray) VALUES (?, ?)";
      return conn.preparedQuery( insertSql ).execute( Tuple.of(1, stringsArray) );
    }).onComplete( ctx.asyncAssertSuccess() );
  }

  @After
  public void tearDown(TestContext ctx) throws Exception {
    pool.close(ctx.asyncAssertSuccess());
  }
}

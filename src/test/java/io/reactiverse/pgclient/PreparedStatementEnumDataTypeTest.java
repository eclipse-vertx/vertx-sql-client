package io.reactiverse.pgclient;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public class PreparedStatementEnumDataTypeTest extends DataTypeTestBase {

  enum Weather{
    sunny,cloudy,rainy;
  }

  enum Mood{
    unhappy,ok,happy;
  }

  @Override
  protected PgConnectOptions options() {
    return options;
  }

  @Test
  public void testInsertEnumDataType(TestContext ctx) {
    Async async = ctx.async(1);
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery("insert into \"EnumDataType\" (id,\"currentMood\",\"currentWeather\") values ($1,$2,$3)", Tuple.of(7, Mood.unhappy, Weather.cloudy),
          ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.rowCount());
            async.countDown();
            async.complete();
          }));
    }));
  }

  @Test
  public void testSetValueNull(TestContext ctx) {
    Async async = ctx.async(1);
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery("insert into \"EnumDataType\" (id,\"currentMood\",\"currentWeather\") values ($1,$2,$3)", Tuple.of(8,null, null),
          ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.rowCount());
            async.countDown();
            async.complete();
          }));
    }));
  }

}

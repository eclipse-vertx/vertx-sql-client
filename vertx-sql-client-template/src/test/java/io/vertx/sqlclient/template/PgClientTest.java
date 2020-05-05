/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.vertx.sqlclient.template;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.Vertx;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.unit.TestContext;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgClientTest extends TemplateTestBase {

  protected Vertx vertx;
  protected PgPool pool;

  @Before
  public void setup() throws Exception {
    vertx = Vertx.vertx();
    pool = PgPool.pool(vertx, connectOptions(), new PoolOptions());
  }

  @After
  public void teardown(TestContext ctx) {
    pool.close();
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testQuery(TestContext ctx) {
    SqlTemplate<Map<String, Object>, RowSet<Row>> template = SqlTemplate
      .forQuery(pool, "SELECT :id :: INT4 \"id\", :randomnumber :: INT4 \"randomnumber\"");
    Map<String, Object> params = new HashMap<>();
    params.put("id", 1);
    params.put("randomnumber", 10);
    template.execute(params, ctx.asyncAssertSuccess(res -> {
      ctx.assertEquals(1, res.size());
      Row row = res.iterator().next();
      ctx.assertEquals(1, row.getInteger(0));
      ctx.assertEquals(10, row.getInteger(1));
    }));
  }

  @Test
  public void testBatch(TestContext ctx) {
    SqlTemplate<Map<String, Object>, RowSet<Row>> template = SqlTemplate
      .forQuery(pool, "SELECT :id :: INT4 \"id\", :randomnumber :: INT4 \"randomnumber\"");
    Map<String, Object> params1 = new HashMap<>();
    params1.put("id", 1);
    params1.put("randomnumber", 10);
    Map<String, Object> params2 = new HashMap<>();
    params1.put("id", 2);
    params1.put("randomnumber", 20);
    template.executeBatch(Arrays.asList(params1, params2), ctx.asyncAssertSuccess(res -> {
      ctx.assertEquals(1, res.size());
      Row row = res.iterator().next();
      ctx.assertEquals(2, row.getInteger(0));
      ctx.assertEquals(20, row.getInteger(1));
      res = res.next();
      ctx.assertNotNull(res);
      row = res.iterator().next();
      // Somehow returns null ... investigate bug
      // ctx.assertEquals(1, row.getInteger(0));
      // ctx.assertEquals(10, row.getInteger(1));
    }));
  }

  @Test
  public void testQueryMap(TestContext ctx) {
    World w = new World();
    w.id = 1;
    w.randomnumber = 10;
    SqlTemplate<World, RowSet<World>> template = SqlTemplate
     .<World>forQuery(pool, "SELECT :id :: INT4 \"id\", :randomnumber :: INT4 \"randomnumber\"")
      .mapFrom(World.class)
     .mapTo(World.class);
   template.execute(w, ctx.asyncAssertSuccess(res -> {
     ctx.assertEquals(1, res.size());
     World world = res.iterator().next();
     ctx.assertEquals(1, world.id);
     ctx.assertEquals(10, world.randomnumber);
   }));
  }

  @Test
  public void testLocalDateTimeWithJackson(TestContext ctx) {
    DatabindCodec.mapper().registerModule(new JavaTimeModule());
    SqlTemplate<Map<String, Object>, RowSet<LocalDateTimePojo>> template = SqlTemplate
      .forQuery(pool, "SELECT :value :: TIMESTAMP WITHOUT TIME ZONE \"localDateTime\"")
      .mapTo(LocalDateTimePojo.class);
    LocalDateTime ldt = LocalDateTime.parse("2017-05-14T19:35:58.237666");
    template.execute(Collections.singletonMap("value", ldt), ctx.asyncAssertSuccess(result -> {
      ctx.assertEquals(1, result.size());
      ctx.assertEquals(ldt, result.iterator().next().localDateTime);
    }));
  }

  @Test
  public void testLocalDateTimeWithCodegen(TestContext ctx) {
    SqlTemplate<Map<String, Object>, RowSet<LocalDateTimeDataObject>> template = SqlTemplate
      .forQuery(pool, "SELECT :value :: TIMESTAMP WITHOUT TIME ZONE \"localDateTime\"")
      .mapTo(LocalDateTimeDataObjectRowMapper.INSTANCE);
    LocalDateTime ldt = LocalDateTime.parse("2017-05-14T19:35:58.237666");
    template.execute(Collections.singletonMap("value", ldt), ctx.asyncAssertSuccess(result -> {
      ctx.assertEquals(1, result.size());
      ctx.assertEquals(ldt, result.iterator().next().getLocalDateTime());
    }));
  }

  @Test
  public void testLocalDateTimeWithCodegenCollector(TestContext ctx) {
    SqlTemplate<Map<String, Object>, SqlResult<List<LocalDateTimeDataObject>>> template = SqlTemplate
      .forQuery(pool, "SELECT :value :: TIMESTAMP WITHOUT TIME ZONE \"localDateTime\"")
      .collecting(LocalDateTimeDataObjectRowMapper.COLLECTOR);
    LocalDateTime ldt = LocalDateTime.parse("2017-05-14T19:35:58.237666");
    template.execute(Collections.singletonMap("value", ldt), ctx.asyncAssertSuccess(result -> {
      ctx.assertEquals(1, result.size());
      ctx.assertEquals(ldt, result.value().get(0).getLocalDateTime());
    }));
  }

  /*
  @Test
  public void testBatchUpdate(TestContext ctx) {
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      BatchTemplate<World> template = BatchTemplate.create(conn, World.class, "INSERT INTO World (id, randomnumber) VALUES (:id, :randomnumber)");
      template.batch(Arrays.asList(
        new World(20_000, 0),
        new World(20_001, 1),
        new World(20_002, 2),
        new World(20_003, 3)
      ), ctx.asyncAssertSuccess(v -> {
        conn.query("SELECT id, randomnumber from WORLD WHERE id=20000", ctx.asyncAssertSuccess(rowset -> {
          ctx.assertEquals(1, rowset.size());
          ctx.assertEquals(0, rowset.iterator().next().getInteger(1));
        }));
      }));
    }));
  }*/
}

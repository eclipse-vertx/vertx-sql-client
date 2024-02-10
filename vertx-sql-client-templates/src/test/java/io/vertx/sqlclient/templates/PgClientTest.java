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

package io.vertx.sqlclient.templates;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.unit.TestContext;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.data.Path;
import io.vertx.pgclient.data.Point;
import io.vertx.sqlclient.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgClientTest extends PgTemplateTestBase {

  private static final String POSTGRES_13_11 = "postgres:13.11";
  private static JdbcDatabaseContainer postgres;
  private static String dbConnectionChain;
  private static String userName;
  private static String password;

  @BeforeClass
  public static void beforeAll() {
    postgres = new PostgreSQLContainer(POSTGRES_13_11).withInitScript("postgresql_create.sql");
    postgres.start();

    dbConnectionChain = postgres.getJdbcUrl();
    userName = postgres.getUsername();
    password = postgres.getPassword();
  }

  @Test
  public void testQuery(TestContext ctx) {
    SqlTemplate<Map<String, Object>, RowSet<Row>> template = SqlTemplate
      .forQuery(connection, "SELECT #{id} :: INT4 \"id\", #{randomnumber} :: INT4 \"randomnumber\"");
    Map<String, Object> params = new HashMap<>();
    params.put("id", 1);
    params.put("randomnumber", 10);
    template
      .execute(params)
      .onComplete(ctx.asyncAssertSuccess(res -> {
      ctx.assertEquals(1, res.size());
      Row row = res.iterator().next();
      ctx.assertEquals(1, row.getInteger(0));
      ctx.assertEquals(10, row.getInteger(1));
    }));
  }

  @Test
  public void testBatch(TestContext ctx) {
    SqlTemplate<Map<String, Object>, RowSet<Row>> template = SqlTemplate
      .forQuery(connection, "SELECT #{id} :: INT4 \"id\", #{randomnumber} :: INT4 \"randomnumber\"");
    Map<String, Object> params1 = new HashMap<>();
    params1.put("id", 1);
    params1.put("randomnumber", 10);
    Map<String, Object> params2 = new HashMap<>();
    params1.put("id", 2);
    params1.put("randomnumber", 20);
    template
      .executeBatch(Arrays.asList(params1, params2))
      .onComplete(ctx.asyncAssertSuccess(res -> {
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
     .<World>forQuery(connection, "SELECT #{id} :: INT4 \"id\", #{randomnumber} :: INT4 \"randomnumber\"")
      .mapFrom(World.class)
     .mapTo(World.class);
   template
     .execute(w)
     .onComplete(ctx.asyncAssertSuccess(res -> {
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
      .forQuery(connection, "SELECT #{value} :: TIMESTAMP WITHOUT TIME ZONE \"localDateTime\"")
      .mapTo(LocalDateTimePojo.class);
    LocalDateTime ldt = LocalDateTime.parse("2017-05-14T19:35:58.237666");
    template
      .execute(Collections.singletonMap("value", ldt))
      .onComplete(ctx.asyncAssertSuccess(result -> {
      ctx.assertEquals(1, result.size());
      ctx.assertEquals(ldt, result.iterator().next().localDateTime);
    }));
  }


  @Test
  public void testLocalDateTimeWithJackson_bis(TestContext ctx) {
    DatabindCodec.mapper().registerModule(new JavaTimeModule());
    RevokedKey revoked = new RevokedKey("hashy", LocalDateTime.parse("2017-05-14T19:35:58.237666"));
    PgConnectOptions connectOptions = new PgConnectOptions()
      .setPort(postgres.getMappedPort(5432))
      .setHost(postgres.getHost())
      .setDatabase(postgres.getDatabaseName())
      .setUser(postgres.getUsername())
      .setPassword(postgres.getPassword());

    Pool pgPool = Pool.pool(vertx, connectOptions, new PoolOptions());

    SqlTemplate
      .forUpdate(pgPool, "INSERT INTO revokedkeys (keyhash, valid_until) VALUES (#{keyhash}, #{valid_until})")
      .mapFrom(RevokedKey.class)
      .execute(revoked)
      .map(r -> revoked.getKeyhash())
      .onComplete(ctx.asyncAssertSuccess(result -> {
        ctx.assertNotNull(result);
      }));
  }

  @Test
  public void testLocalDateTimeWithCodegen(TestContext ctx) {
    SqlTemplate<Map<String, Object>, RowSet<LocalDateTimeDataObject>> template = SqlTemplate
      .forQuery(connection, "SELECT #{value} :: TIMESTAMP WITHOUT TIME ZONE \"localDateTime\"")
      .mapTo(LocalDateTimeDataObjectRowMapper.INSTANCE);
    LocalDateTime ldt = LocalDateTime.parse("2017-05-14T19:35:58.237666");
    template
      .execute(Collections.singletonMap("value", ldt))
      .onComplete(ctx.asyncAssertSuccess(result -> {
      ctx.assertEquals(1, result.size());
      ctx.assertEquals(ldt, result.iterator().next().getLocalDateTime());
    }));
  }

  @Test
  public void testLocalDateTimeWithCodegenCollector(TestContext ctx) {
    SqlTemplate<Map<String, Object>, SqlResult<List<LocalDateTimeDataObject>>> template = SqlTemplate
      .forQuery(connection, "SELECT #{value} :: TIMESTAMP WITHOUT TIME ZONE \"localDateTime\"")
      .collecting(LocalDateTimeDataObjectRowMapper.COLLECTOR);
    LocalDateTime ldt = LocalDateTime.parse("2017-05-14T19:35:58.237666");
    template
      .execute(Collections.singletonMap("value", ldt))
      .onComplete(ctx.asyncAssertSuccess(result -> {
      ctx.assertEquals(1, result.size());
      ctx.assertEquals(ldt, result.value().get(0).getLocalDateTime());
    }));
  }

  @Test
  public void testDataTypes(TestContext ctx) {
    Point point = new Point().setX(4).setY(7);
    Path path = new Path().addPoint(point);
    testGet(ctx, "POINT", point, PostgreSQLDataObject::getPoint, "point");
    testGet(ctx, "PATH", path, PostgreSQLDataObject::getPath, "path");
  }

  private <V> void testGet(TestContext ctx, String sqlType, V value, Function<PostgreSQLDataObject, V> extractor, String column) {
    super.testGet(
      ctx,
      sqlType,
      PostgreSQLDataObjectRowMapper.INSTANCE,
      TupleMapper.mapper(Function.identity()),
      "value",
      Collections.singletonMap("value", value),
      value,
      extractor,
      column);
  }

  @Test
  public void testAnemicJson(TestContext ctx) {
    SqlTemplate<JsonObject, RowSet<Row>> template = SqlTemplate
      .forQuery(connection, "SELECT " +
        "#{integer} :: INT4 \"integer\", " +
        "#{boolean} :: BOOL \"boolean\", " +
        "#{string} :: VARCHAR \"string\"")
      .mapFrom(TupleMapper.jsonObject());
    JsonObject params = new JsonObject()
      .put("integer", 4)
      .put("string", "hello world")
      .put("boolean", true);
    template
      .execute(params)
      .onComplete(ctx.asyncAssertSuccess(res -> {
      ctx.assertEquals(1, res.size());
      Row row = res.iterator().next();
      ctx.assertEquals(4, row.getInteger(0));
      ctx.assertEquals(true, row.getBoolean(1));
      ctx.assertEquals("hello world", row.getString(2));
    }));
  }

  @Test
  public void testInsertJsonObject(TestContext ctx) {
    connection
      .query("DROP TABLE IF EXISTS distributors")
      .execute()
      .onComplete(ctx.asyncAssertSuccess(dropped -> {
      connection
        .query("CREATE TABLE distributors(name VARCHAR(40), attrs JSONB)")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(created -> {

        MyObject value = new MyObject();
        value.setName("foo");

        Attributes attributes = new Attributes();
        Instant createdOn = Instant.now();
        attributes.setCreatedOn(createdOn);
        List<String> stringAttributes = Arrays.asList("foo", "bar", "baz");
        attributes.setStringAttributes(stringAttributes);
        value.setAttributes(attributes);

        SqlTemplate
          .forQuery(connection, "INSERT INTO distributors (name,attrs) VALUES(#{name},#{attributes})")
          .mapFrom(MyObject.class)
          .execute(value)
          .onComplete(ctx.asyncAssertSuccess(inserted -> {
            connection
              .query("SELECT name, attrs FROM distributors")
              .execute()
              .onComplete(ctx.asyncAssertSuccess(rows -> {
              ctx.verify(v -> {
                assertEquals(1, rows.size());
                Row row = rows.iterator().next();
                assertEquals("foo", row.getValue("name"));
                Object object = row.getValue("attrs");
                assertTrue(object instanceof JsonObject);
                JsonObject attrs = (JsonObject) object;
                assertEquals(createdOn, attrs.getInstant("createdOn"));
                assertEquals(stringAttributes, attrs.getJsonArray("stringAttributes").getList());
              });
            }));
          }));
      }));
    }));
  }

  @SuppressWarnings("unused")
  private static class MyObject {

    private String name;
    private Attributes attributes;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Attributes getAttributes() {
      return attributes;
    }

    public void setAttributes(Attributes attributes) {
      this.attributes = attributes;
    }

    @Override
    public String toString() {
      return "MyObject{" +
        "name='" + name + '\'' +
        ", attributes=" + attributes +
        '}';
    }
  }

  @SuppressWarnings("unused")
  private static class Attributes {

    private Instant createdOn;
    private List<String> stringAttributes;

    public Instant getCreatedOn() {
      return createdOn;
    }

    public void setCreatedOn(Instant createdOn) {
      this.createdOn = createdOn;
    }

    public List<String> getStringAttributes() {
      return stringAttributes;
    }

    public void setStringAttributes(List<String> stringAttributes) {
      this.stringAttributes = stringAttributes;
    }

    @Override
    public String toString() {
      return "Attributes{" +
        "createdOn=" + createdOn +
        ", stringAttributes=" + stringAttributes +
        '}';
    }
  }

  public class RevokedKey {
    private String keyhash;
    private LocalDateTime valid_until;

    public RevokedKey(String keyhash, LocalDateTime valid_until) {
      this.keyhash = keyhash;
      this.valid_until = valid_until;
    }

    public RevokedKey(){}

    public String getKeyhash() {
      return keyhash;
    }

    public void setKeyhash(String keyhash) {
      this.keyhash = keyhash;
    }

    public LocalDateTime getValid_until() {
      return valid_until;
    }

    public void setValid_until(LocalDateTime valid_until) {
      this.valid_until = valid_until;
    }
  }
}

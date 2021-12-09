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
package io.vertx.pgclient;

import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlConnectOptions;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceConfigurationError;

import static org.junit.Assert.fail;

/**
 * @author Billy Yuan <billy112487983@gmail.com>
 */
public class PgConnectOptionsTest {
  private String connectionUri;
  private PgConnectOptions expectedConfiguration;
  private PgConnectOptions actualConfiguration;

  @Test
  public void testValidUri1() {
    connectionUri = "postgresql://";
    actualConfiguration = PgConnectOptions.fromUri(connectionUri);

    expectedConfiguration = new PgConnectOptions();

    assertEquals(expectedConfiguration, actualConfiguration);
  }

  @Test
  public void testValidUri2() {
    connectionUri = "postgresql://myhost";
    actualConfiguration = PgConnectOptions.fromUri(connectionUri);

    expectedConfiguration = new PgConnectOptions()
      .setHost("myhost");

    assertEquals(expectedConfiguration, actualConfiguration);
  }

  @Test
  public void testValidUri3() {
    connectionUri = "postgresql://myhost:5433";
    actualConfiguration = PgConnectOptions.fromUri(connectionUri);

    expectedConfiguration = new PgConnectOptions()
      .setHost("myhost")
      .setPort(5433);

    assertEquals(expectedConfiguration, actualConfiguration);
  }

  @Test
  public void testValidUri4() {
    connectionUri = "postgresql://myhost/mydb";
    actualConfiguration = PgConnectOptions.fromUri(connectionUri);

    expectedConfiguration = new PgConnectOptions()
      .setHost("myhost")
      .setDatabase("mydb");

    assertEquals(expectedConfiguration, actualConfiguration);
  }

  @Test
  public void testValidUri5() {
    connectionUri = "postgresql://user@myhost";
    actualConfiguration = PgConnectOptions.fromUri(connectionUri);

    expectedConfiguration = new PgConnectOptions()
      .setUser("user")
      .setHost("myhost");

    assertEquals(expectedConfiguration, actualConfiguration);
  }

  @Test
  public void testValidUri6() {
    connectionUri = "postgresql://user:secret@myhost";
    actualConfiguration = PgConnectOptions.fromUri(connectionUri);

    expectedConfiguration = new PgConnectOptions()
      .setUser("user")
      .setPassword("secret")
      .setHost("myhost");

    assertEquals(expectedConfiguration, actualConfiguration);
  }

  @Test
  public void testValidUri7() {
    connectionUri = "postgresql://other@localhost/otherdb?port=5433&password=secret";
    actualConfiguration = PgConnectOptions.fromUri(connectionUri);

    expectedConfiguration = new PgConnectOptions()
      .setUser("other")
      .setPassword("secret")
      .setHost("localhost")
      .setPort(5433)
      .setDatabase("otherdb");

    assertEquals(expectedConfiguration, actualConfiguration);
  }

  @Test
  public void testValidUri8() {
    connectionUri = "postgresql:///dbname?host=/var/lib/postgresql";
    actualConfiguration = PgConnectOptions.fromUri(connectionUri);

    expectedConfiguration = new PgConnectOptions()
      .setHost("/var/lib/postgresql")
      .setDatabase("dbname");

    assertEquals(expectedConfiguration, actualConfiguration);
  }

  @Test
  public void testValidUri9() {
    connectionUri = "postgresql://%2Fvar%2Flib%2Fpostgresql/dbname";
    actualConfiguration = PgConnectOptions.fromUri(connectionUri);

    expectedConfiguration = new PgConnectOptions()
      .setHost("/var/lib/postgresql")
      .setDatabase("dbname");

    assertEquals(expectedConfiguration, actualConfiguration);
  }

  @Test
  public void testValidUri10() {
    connectionUri = "postgresql://user@myhost?sslmode=require";
    actualConfiguration = PgConnectOptions.fromUri(connectionUri);

    expectedConfiguration = new PgConnectOptions()
      .setHost("myhost")
      .setUser("user")
      .setSslMode(SslMode.REQUIRE);

    assertEquals(expectedConfiguration, actualConfiguration);
  }

  @Test
  public void testValidUri11() {
    connectionUri = "postgresql://user@myhost?application_name=myapp";
    actualConfiguration = PgConnectOptions.fromUri(connectionUri);

    Map<String, String> expectedProperties = new HashMap<>();
    expectedProperties.put("application_name", "myapp");

    expectedConfiguration = new PgConnectOptions()
      .setHost("myhost")
      .setUser("user")
      .setProperties(expectedProperties);

    assertEquals(expectedConfiguration, actualConfiguration);
  }

  @Test
  public void testValidUri12() {
    connectionUri = "postgresql://?fallback_application_name=myapp&search_path=myschema";
    actualConfiguration = PgConnectOptions.fromUri(connectionUri);

    Map<String, String> expectedProperties = new HashMap<>();
    expectedProperties.put("fallback_application_name", "myapp");
    expectedProperties.put("search_path", "myschema");

    expectedConfiguration = new PgConnectOptions()
      .setProperties(expectedProperties);

    assertEquals(expectedConfiguration, actualConfiguration);
  }


  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUri1() {
    connectionUri = "postgrsql://username";
    actualConfiguration = PgConnectOptions.fromUri(connectionUri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUri2() {
    connectionUri = "postgresql://username:password@loc//dbname";
    actualConfiguration = PgConnectOptions.fromUri(connectionUri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUri3() {
    connectionUri = "postgresql://user@:passowrd@localhost/dbname/qwer";
    actualConfiguration = PgConnectOptions.fromUri(connectionUri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUri4() {
    connectionUri = "postgresql://user:password@localhost:655355/dbname";
    actualConfiguration = PgConnectOptions.fromUri(connectionUri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUri5() {
    connectionUri = "postgresql://user@localhost?port=1234&port";
    actualConfiguration = PgConnectOptions.fromUri(connectionUri);
  }

  @Test
  public void testMerge() {
    PgConnectOptions options = new PgConnectOptions();
    options.setUser("the-user");
    options.setPassword("the-password");
    options.setReconnectAttempts(3);
    JsonObject conf = new JsonObject();
    conf.put("database", "the-database");
    conf.put("host", "the-host");
    conf.put("port", 1234);
    conf.put("port", 1234);
    conf.put("pipeliningLimit", 5);
    conf.put("reconnectInterval", 10);
    options = options.merge(conf);
    Assert.assertEquals("the-user", options.getUser());
    Assert.assertEquals("the-password", options.getPassword());
    Assert.assertEquals("the-database", options.getDatabase());
    Assert.assertEquals("the-host", options.getHost());
    Assert.assertEquals(1234, options.getPort());
    Assert.assertEquals(5, options.getPipeliningLimit());
    Assert.assertEquals(10, options.getReconnectInterval());
    Assert.assertEquals(3, options.getReconnectAttempts());
  }

  @Test
  public void testGeneric() {
    PgConnectOptions options = (PgConnectOptions) SqlConnectOptions.fromUri("postgresql://myhost:5433");
    Assert.assertEquals("myhost", options.getHost());
    Assert.assertEquals(5433, options.getPort());
    try {
      SqlConnectOptions.fromUri("postgresql://username:password@loc//dbname");
      fail();
    } catch (IllegalArgumentException ignore) {
    }
    try {
      SqlConnectOptions.fromUri("whatever://myhost:5433");
      fail();
    } catch (ServiceConfigurationError ignore) {
    }
  }

  private static void assertEquals(PgConnectOptions expectedConfiguration, PgConnectOptions actualConfiguration) {
    Assert.assertEquals(expectedConfiguration.toJson(), actualConfiguration.toJson());
  }
}

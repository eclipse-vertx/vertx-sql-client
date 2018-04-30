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
package io.reactiverse.pgclient;

import io.vertx.core.json.JsonObject;
import org.junit.Test;

import static io.reactiverse.pgclient.impl.PgConnectionUriParser.*;
import static org.junit.Assert.*;

/**
 * @author Billy Yuan <billy112487983@gmail.com>
 */
public class PgConnectionUriParserTest {
  private String uri;
  private JsonObject actualParsedResult;
  private JsonObject expectedParsedResult;

  @Test
  public void testParsingUriSchemeDesignator() {
    uri = "postgresql://";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject();

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingAnotherUriSchemeDesignator() {
    uri = "postgres://";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject();

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingInvalidUriSchemeDesignator() {
    uri = "posttgres://localhost";
    actualParsedResult = parse(uri);
  }

  @Test
  public void testParsingUser() {
    uri = "postgres://user@";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("user", "user");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingPassword() {
    uri = "postgresql://user:secret@";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("user", "user")
      .put("password", "secret");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingHost() {
    uri = "postgresql://localhost";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("host", "localhost");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingIpv4Address() {
    uri = "postgresql://192.168.1.1";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("host", "192.168.1.1");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingIpv6Address() {
    uri = "postgresql://[2001:db8::1234]";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("host", "2001:db8::1234");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingPort() {
    uri = "postgresql://:1234";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("port", 1234);

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingDbName() {
    uri = "postgres:///mydb";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("database", "mydb");

    assertEquals(expectedParsedResult, actualParsedResult);
  }


  @Test
  public void testParsingOneParameter() {
    uri = "postgresql://?user=other";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("user", "other");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingParameters() {
    uri = "postgresql://?user=other&password=secret&port=1234";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("user", "other")
      .put("password", "secret")
      .put("port", 1234);

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingHostAndParameters() {
    uri = "postgresql://localhost?user=other&password=secret";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("host", "localhost")
      .put("user", "other")
      .put("password", "secret");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingUserWithoutPassword() {
    uri = "postgresql://user@";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("user", "user");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingPasswordWithoutUser() {
    uri = "postgresql://:secret@";
    actualParsedResult = parse(uri);
  }

  @Test
  public void testParsingHostWithPort() {
    uri = "postgresql://localhost:1234";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("host", "localhost")
      .put("port", 1234);

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingPortAndDbName() {
    uri = "postgresql://:1234/mydb";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("port", 1234)
      .put("database", "mydb");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingUserAndParameters() {
    uri = "postgresql://user@?host=localhost&port=1234";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("user", "user")
      .put("host", "localhost")
      .put("port", 1234);

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingDomainSocket() {
    uri = "postgresql://%2Fvar%2Flib%2Fpostgresql";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("host", "/var/lib/postgresql");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingDomainSocketInParameter() {
    uri = "postgresql://?host=/var/lib/postgresql";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("host", "/var/lib/postgresql");

    assertEquals(expectedParsedResult, actualParsedResult);
  }


  @Test
  public void testParsingUriWithOverridenParameters() {
    uri = "postgresql://localhost/mydb?host=myhost&port=1234";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("host", "myhost")
      .put("database", "mydb")
      .put("port", 1234);

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingFullUri() {
    uri = "postgresql://dbuser:secretpassword@database.server.com:3211/mydb";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("user", "dbuser")
      .put("password", "secretpassword")
      .put("host", "database.server.com")
      .put("port", 3211)
      .put("database", "mydb");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingInvalidUri1() {
    uri = "postgresql://us@er@@";
    actualParsedResult = parse(uri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingInvalidUri2() {
    uri = "postgresql://user/mydb//";
    actualParsedResult = parse(uri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingInvalidUri3() {
    uri = "postgresql:///dbname/?host=localhost";
    actualParsedResult = parse(uri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingInvalidUri4() {
    uri = "postgresql://user::1234";
    actualParsedResult = parse(uri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingInvalidUri5() {
    uri = "postgresql://@:1234";
    actualParsedResult = parse(uri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingInvalidUri6() {
    uri = "postgresql://:123:";
    actualParsedResult = parse(uri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingInvalidUri7() {
    uri = "postgresql://@@/dbname?host";
    actualParsedResult = parse(uri);
  }
}

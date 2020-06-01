/*
 * Copyright (c) 2020 IBM Corporation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.db2client;

import io.vertx.core.json.JsonObject;
import org.junit.Test;

import static io.vertx.db2client.impl.DB2ConnectionUriParser.parse;
import static org.junit.Assert.*;

public class DB2ConnectionUriParserTest {
  private String uri;
  private JsonObject actualParsedResult;
  private JsonObject expectedParsedResult;

  @Test
  public void testParsingUriSchemeDesignator() {
    uri = "db2://localhost";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject().put("host", "localhost");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingInvalidUriSchemeDesignator() {
    uri = "db2e://localhost";
    actualParsedResult = parse(uri);
  }

  @Test
  public void testParsingUserWithoutPassword() {
    uri = "db2://user@localhost";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("user", "user")
      .put("host", "localhost");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingUserWithEmptyPassword() {
    uri = "db2://user:@localhost";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("user", "user")
      .put("password", "")
      .put("host", "localhost");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingUserWithPassword() {
    uri = "db2://user:secret@localhost";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("user", "user")
      .put("password", "secret")
      .put("host", "localhost");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingPasswordWithoutUser() {
    uri = "db2://:secret@localhost";
    actualParsedResult = parse(uri);
  }

  @Test
  public void testParsingHostName() {
    uri = "db2://vertx.io";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("host", "vertx.io");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingIpv4Address() {
    uri = "db2://192.168.1.1";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("host", "192.168.1.1");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingIpv6Address() {
    uri = "db2://[2001:db8::1234]";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("host", "2001:db8::1234");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingPort() {
    uri = "db2://localhost:1234";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("host", "localhost")
      .put("port", 1234);

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingSchemaName() {
    uri = "db2://localhost/mydb";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("host", "localhost")
      .put("database", "mydb");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingOneAttribute() {
    uri = "db2://localhost?user=other";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("host", "localhost")
      .put("user", "other");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingParameters() {
    uri = "db2://localhost?user=other&password=secret&port=1234";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("host", "localhost")
      .put("user", "other")
      .put("password", "secret")
      .put("port", 1234);

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingHostWithPort() {
    uri = "db2://localhost:1234";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("host", "localhost")
      .put("port", 1234);

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingEncodedDB() {
    uri = "db2://user_name@198.51.100.2:3306/world%5Fx";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("user", "user_name")
      .put("host", "198.51.100.2")
      .put("port", 3306)
      .put("database", "world_x");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingInvalidUri1() {
    uri = "db2://us@er@@";
    actualParsedResult = parse(uri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingInvalidUri2() {
    uri = "db2://user/mydb//";
    actualParsedResult = parse(uri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingInvalidUri3() {
    uri = "db2:///dbname/?host=localhost";
    actualParsedResult = parse(uri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingInvalidUri4() {
    uri = "db2://user::1234";
    actualParsedResult = parse(uri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingInvalidUri5() {
    uri = "db2://@:1234";
    actualParsedResult = parse(uri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingInvalidUri6() {
    uri = "db2://:123:";
    actualParsedResult = parse(uri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingInvalidUri7() {
    uri = "db2://@@/dbname?host";
    actualParsedResult = parse(uri);
  }

  @Test
  public void testParsingUserInfoContainExclamationMark(){
      uri = "db2://user!name:dd!dd@127.0.0.1:3306/dbname";
      actualParsedResult = parse(uri);
  }

}

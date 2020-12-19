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

package io.vertx.mssqlclient.impl;

import io.vertx.core.json.JsonObject;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static io.vertx.mssqlclient.impl.MSSQLConnectionUriParser.parse;
import static org.junit.Assert.assertEquals;

public class MSSQLConnectionUriParserTest {
  private String uri;
  private JsonObject actualParsedResult;
  private JsonObject expectedParsedResult;

  @Test
  public void testParsingUriSchemeDesignator() {
    uri = "sqlserver://localhost";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject().put("host", "localhost");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingInvalidUriSchemeDesignator() {
    uri = "sqlservere://localhost";
    actualParsedResult = parse(uri);
  }

  @Test
  public void testParsingUserWithoutPassword() {
    uri = "sqlserver://user@localhost";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("user", "user")
      .put("host", "localhost");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingUserWithEmptyPassword() {
    uri = "sqlserver://user:@localhost";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("user", "user")
      .put("password", "")
      .put("host", "localhost");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingUserWithPassword() {
    uri = "sqlserver://user:secret@localhost";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("user", "user")
      .put("password", "secret")
      .put("host", "localhost");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingPasswordWithoutUser() {
    uri = "sqlserver://:secret@localhost";
    actualParsedResult = parse(uri);
  }

  @Test
  public void testParsingHostName() {
    uri = "sqlserver://vertx.io";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("host", "vertx.io");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingIpv4Address() {
    uri = "sqlserver://192.168.1.1";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("host", "192.168.1.1");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingIpv6Address() {
    uri = "sqlserver://[2001:db8::1234]";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("host", "2001:db8::1234");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingPort() {
    uri = "sqlserver://localhost:1234";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("host", "localhost")
      .put("port", 1234);

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingSchemaName() {
    uri = "sqlserver://localhost/mydb";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("host", "localhost")
      .put("database", "mydb");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingOneAttribute() {
    uri = "sqlserver://localhost?user=other";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("host", "localhost")
      .put("user", "other");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingParameters() {
    uri = "sqlserver://localhost?user=other&password=secret&port=1234";
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
    uri = "sqlserver://localhost:1234";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("host", "localhost")
      .put("port", 1234);

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingEncodedSchema() {
    uri = "sqlserver://user_name@198.51.100.2:3306/world%5Fx";
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
    uri = "sqlserver://us@er@@";
    actualParsedResult = parse(uri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingInvalidUri2() {
    uri = "sqlserver://user/mydb//";
    actualParsedResult = parse(uri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingInvalidUri3() {
    uri = "sqlserver:///dbname/?host=localhost";
    actualParsedResult = parse(uri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingInvalidUri4() {
    uri = "sqlserver://user::1234";
    actualParsedResult = parse(uri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingInvalidUri5() {
    uri = "sqlserver://@:1234";
    actualParsedResult = parse(uri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingInvalidUri6() {
    uri = "sqlserver://:123:";
    actualParsedResult = parse(uri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingInvalidUri7() {
    uri = "sqlserver://@@/dbname?host";
    actualParsedResult = parse(uri);
  }

  @Test
  public void testParsingUserInfoContainExclamationMark(){
    uri = "sqlserver://user!name:dd!dd@127.0.0.1:3306/dbname";
    actualParsedResult = parse(uri);
  }

  @Test
  public void testParsingSchemaContainExclamationMark(){
    uri = "sqlserver://username:dddd@127.0.0.1:3306/!dbname";
    actualParsedResult = parse(uri);
  }
}

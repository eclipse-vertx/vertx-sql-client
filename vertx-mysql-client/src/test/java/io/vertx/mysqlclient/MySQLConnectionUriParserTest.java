package io.vertx.mysqlclient;

import io.vertx.core.json.JsonObject;
import org.junit.Test;

import static io.vertx.mysqlclient.impl.MySQLConnectionUriParser.*;
import static org.junit.Assert.*;

public class MySQLConnectionUriParserTest {
  private String uri;
  private JsonObject actualParsedResult;
  private JsonObject expectedParsedResult;

  @Test
  public void testParsingUriSchemeDesignator() {
    uri = "mysql://localhost";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject().put("host", "localhost");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingInvalidUriSchemeDesignator() {
    uri = "mysqle://localhost";
    actualParsedResult = parse(uri);
  }

  @Test
  public void testParsingUserWithoutPassword() {
    uri = "mysql://user@localhost";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("user", "user")
      .put("host", "localhost");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingUserWithEmptyPassword() {
    uri = "mysql://user:@localhost";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("user", "user")
      .put("password", "")
      .put("host", "localhost");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingUserWithPassword() {
    uri = "mysql://user:secret@localhost";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("user", "user")
      .put("password", "secret")
      .put("host", "localhost");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingPasswordWithoutUser() {
    uri = "mysql://:secret@localhost";
    actualParsedResult = parse(uri);
  }

  @Test
  public void testParsingHostName() {
    uri = "mysql://vertx.io";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("host", "vertx.io");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingIpv4Address() {
    uri = "mysql://192.168.1.1";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("host", "192.168.1.1");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingIpv6Address() {
    uri = "mysql://[2001:db8::1234]";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("host", "2001:db8::1234");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingPort() {
    uri = "mysql://localhost:1234";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("host", "localhost")
      .put("port", 1234);

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingSchemaName() {
    uri = "mysql://localhost/mydb";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("host", "localhost")
      .put("database", "mydb");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingOneAttribute() {
    uri = "mysql://localhost?user=other";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("host", "localhost")
      .put("user", "other");

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingParameters() {
    uri = "mysql://localhost?user=other&password=secret&port=1234";
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
    uri = "mysql://localhost:1234";
    actualParsedResult = parse(uri);

    expectedParsedResult = new JsonObject()
      .put("host", "localhost")
      .put("port", 1234);

    assertEquals(expectedParsedResult, actualParsedResult);
  }

  @Test
  public void testParsingEncodedSchema() {
    uri = "mysql://user_name@198.51.100.2:3306/world%5Fx";
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
    uri = "mysql://us@er@@";
    actualParsedResult = parse(uri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingInvalidUri2() {
    uri = "mysql://user/mydb//";
    actualParsedResult = parse(uri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingInvalidUri3() {
    uri = "mysql:///dbname/?host=localhost";
    actualParsedResult = parse(uri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingInvalidUri4() {
    uri = "mysql://user::1234";
    actualParsedResult = parse(uri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingInvalidUri5() {
    uri = "mysql://@:1234";
    actualParsedResult = parse(uri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingInvalidUri6() {
    uri = "mysql://:123:";
    actualParsedResult = parse(uri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsingInvalidUri7() {
    uri = "mysql://@@/dbname?host";
    actualParsedResult = parse(uri);
  }
}

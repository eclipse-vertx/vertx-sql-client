package io.reactiverse.pgclient;

import org.junit.Test;

import static io.reactiverse.pgclient.PgConnectOptionsProvider.*;
import static org.junit.Assert.*;

/**
 * @author Billy Yuan <billy112487983@gmail.com>
 */
public class PgConnectionUriParserTest {
  private String uri;
  private PgConnectOptions actualParsedOptions;
  private PgConnectOptions expectedParsedOptions;

  @Test
  public void testParsingUriSchemeDesignator() {
    uri = "postgresql://";
    actualParsedOptions = fromUri(uri);

    expectedParsedOptions = new PgConnectOptions();

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingAnotherUriSchemeDesignator() {
    uri = "postgres://";
    actualParsedOptions = fromUri(uri);

    expectedParsedOptions = new PgConnectOptions();

    assertEquals(expectedParsedOptions, actualParsedOptions);

  }

  @Test
  public void testParsingWrongUriSchemeDesignator() {
    uri = "posttgres://localhost";
    actualParsedOptions = fromUri(uri);

    expectedParsedOptions = new PgConnectOptions();

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingUsername() {
    uri = "postgres://user@";
    actualParsedOptions = fromUri(uri);

    expectedParsedOptions = new PgConnectOptions()
      .setUsername("user");

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingPassword() {
    uri = "postgresql://user:secret@localhost";
    actualParsedOptions = fromUri(uri);

    expectedParsedOptions = new PgConnectOptions()
      .setUsername("user")
      .setPassword("secret")
      .setHost("localhost");

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingHost() {
    uri = "postgresql://localhost";
    actualParsedOptions = fromUri(uri);

    expectedParsedOptions = new PgConnectOptions()
      .setHost("localhost");

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingIpv4Address() {
    uri = "postgresql://192.168.1.1:1234";
    actualParsedOptions = fromUri(uri);

    expectedParsedOptions = new PgConnectOptions()
      .setHost("192.168.1.1")
      .setPort(1234);

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingIpv6Address() {
    uri = "postgresql://[2001:db8::1234]/mydb";
    actualParsedOptions = fromUri(uri);

    expectedParsedOptions = new PgConnectOptions()
      .setHost("2001:db8::1234")
      .setDatabase("mydb");

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingPort() {
    uri = "postgresql://localhost:1234";
    actualParsedOptions = fromUri(uri);

    expectedParsedOptions = new PgConnectOptions()
      .setHost("localhost")
      .setPort(1234);

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }


  @Test
  public void testParsingDbName() {
    uri = "postgres://localhost/mydb";
    actualParsedOptions = fromUri(uri);

    expectedParsedOptions = new PgConnectOptions()
      .setHost("localhost")
      .setDatabase("mydb");

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }


  @Test
  public void testParsingParameter() {
    uri = "postgresql://localhost/otherdb?user=other";
    actualParsedOptions = fromUri(uri);

    expectedParsedOptions = new PgConnectOptions()
      .setUsername("other")
      .setHost("localhost")
      .setDatabase("otherdb");

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingParameters() {
    uri = "postgresql://localhost/otherdb?user=other&password=secret&port=1234";
    actualParsedOptions = fromUri(uri);

    expectedParsedOptions = new PgConnectOptions()
      .setHost("localhost")
      .setDatabase("otherdb")
      .setUsername("other")
      .setPassword("secret")
      .setPort(1234);

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingHostAndParameters() {
    uri = "postgresql://localhost?user=other&password=secret";
    actualParsedOptions = fromUri(uri);

    expectedParsedOptions = new PgConnectOptions()
      .setHost("localhost")
      .setUsername("other")
      .setPassword("secret");

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingUserWithoutPassword() {
    uri = "postgresql://user@";
    actualParsedOptions = fromUri(uri);

    expectedParsedOptions = new PgConnectOptions()
      .setUsername("user");

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingPasswordWithoutUsername() {
    uri = "postgresql://:secret@";
    actualParsedOptions = fromUri(uri);

    expectedParsedOptions = new PgConnectOptions();

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingPortWithoutHost() {
    // This URI is not valid in java.net.URI
    uri = "postgresql://:1234";
    actualParsedOptions = fromUri(uri);

    expectedParsedOptions = new PgConnectOptions()
      .setPort(1234);

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingOnlyDbName() {
    uri = "postgresql:///mydb";
    actualParsedOptions = fromUri(uri);

    expectedParsedOptions = new PgConnectOptions()
      .setDatabase("mydb");

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingOnlyParameters() {
    uri = "postgresql://?host=localhost&port=1234";
    actualParsedOptions = fromUri(uri);

    expectedParsedOptions = new PgConnectOptions()
      .setHost("localhost")
      .setPort(1234);

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingDomainSocket() {
    uri = "postgresql://%2Fvar%2Flib%2Fpostgresql/dbname";
    actualParsedOptions = fromUri(uri);

    expectedParsedOptions = new PgConnectOptions()
      .setDatabase("dbname");

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingDomainSocketInParameter() {
    uri = "postgresql:///dbname?host=/var/lib/postgresql";
    actualParsedOptions = fromUri(uri);

    expectedParsedOptions = new PgConnectOptions()
      .setDatabase("dbname");

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingInvalidUri() {
    uri = "postgresql://@@/dbname?host";
    actualParsedOptions = fromUri(uri);

    expectedParsedOptions = new PgConnectOptions();

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingUriWithOverridenParameters() {
    uri = "postgresql://localhost/mydb?host=myhost&port=1234";
    actualParsedOptions = fromUri(uri);

    expectedParsedOptions = new PgConnectOptions()
      .setHost("myhost")
      .setDatabase("mydb")
      .setPort(1234);

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingFullUri() {
    uri = "postgresql://dbuser:secretpassword@database.server.com:3211/mydb";
    actualParsedOptions = fromUri(uri);

    expectedParsedOptions = new PgConnectOptions()
      .setUsername("dbuser")
      .setPassword("secretpassword")
      .setHost("database.server.com")
      .setPort(3211)
      .setDatabase("mydb");

    assertEquals(expectedParsedOptions, actualParsedOptions);

    PgConnectOptions expectedPgConnectOptions = new PgConnectOptions()
      .setUsername("dbuser")
      .setPassword("secretpassword")
      .setHost("database.server.com")
      .setPort(3211)
      .setDatabase("mydb");

    PgConnectOptions actualPgConnectOptions = fromUri(uri);

    assertEquals(expectedPgConnectOptions, actualPgConnectOptions);
  }

  @Test
  public void testProvidingPgConnectOptions() {
    uri = "postgresql://pg@localhost?password=secret123&port=1234";

    PgConnectOptions expectedOptions = new PgConnectOptions()
      .setUsername("pg")
      .setPassword("secret123")
      .setHost("localhost")
      .setPort(1234);

    assertEquals(expectedOptions, fromUri(uri));
  }

  @Test
  public void testProvidingWrongPgConnectOptions() {
    uri = "postgresql://user:secret@localhost/mydb?port=1234";

    PgConnectOptions wrongOptions = new PgConnectOptions()
      .setUsername("pg")
      .setPassword("secret123")
      .setHost("localhost")
      .setPort(1234);

    assertNotEquals(wrongOptions, fromUri(uri));
  }

  @Test
  public void testInvalidUri1() {
    uri = "postgresql://us@er@@";
    actualParsedOptions = fromUri(uri);

    expectedParsedOptions = new PgConnectOptions();

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testInvalidUri2() {
    uri = "postgresql://user/mydb//";
    actualParsedOptions = fromUri(uri);

    expectedParsedOptions = new PgConnectOptions();

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testInvalidUri3() {
    uri = "postgresql:///dbname/?host=localhost";
    actualParsedOptions = fromUri(uri);

    expectedParsedOptions = new PgConnectOptions();

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testInvalidUri4() {
    uri = "postgresql://user::1234";
    actualParsedOptions = fromUri(uri);

    expectedParsedOptions = new PgConnectOptions();

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testInvalidUri5() {
    uri = "postgresql://@:1234";
    actualParsedOptions = fromUri(uri);

    expectedParsedOptions = new PgConnectOptions();

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testInvalidUri6() {
    uri = "postgresql://:123:";
    actualParsedOptions = fromUri(uri);

    expectedParsedOptions = new PgConnectOptions();

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }
}

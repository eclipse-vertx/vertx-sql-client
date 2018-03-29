package io.reactiverse.pgclient;

import io.reactiverse.pgclient.impl.PgConnectionUriParser;
import org.junit.Ignore;
import org.junit.Test;

import static io.reactiverse.pgclient.impl.PgConnectionUriParser.*;
import static org.junit.Assert.*;

/**
 * @author Billy Yuan <billy112487983@gmail.com>
 */
public class PgConnectionUriParserTest {
  private String uri;
  private PgPoolOptions actualParsedOptions;
  private PgPoolOptions expectedParsedOptions;

  @Test
  public void testParsingUriSchemeDesignator() {
    uri = "postgresql://";
    actualParsedOptions = translateToPgPoolOptions(uri);

    expectedParsedOptions = new PgPoolOptions();

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingAnotherUriSchemeDesignator() {
    uri = "postgres://";
    actualParsedOptions = translateToPgPoolOptions(uri);

    expectedParsedOptions = new PgPoolOptions();

    assertEquals(expectedParsedOptions, actualParsedOptions);

  }

  @Test
  public void testParsingWrongUriSchemeDesignator() {
    uri = "posttgres://localhost";
    actualParsedOptions = translateToPgPoolOptions(uri);

    expectedParsedOptions = new PgPoolOptions();

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingUsername() {
    uri = "postgres://user@";
    actualParsedOptions = translateToPgPoolOptions(uri);

    expectedParsedOptions = new PgPoolOptions()
      .setUsername("user");

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingPassword() {
    uri = "postgresql://user:secret@localhost";
    actualParsedOptions = translateToPgPoolOptions(uri);

    expectedParsedOptions = new PgPoolOptions()
      .setUsername("user")
      .setPassword("secret")
      .setHost("localhost");

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingHost() {
    uri = "postgresql://localhost";
    actualParsedOptions = translateToPgPoolOptions(uri);

    expectedParsedOptions = new PgPoolOptions()
      .setHost("localhost");

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingIpv4Address() {
    uri = "postgresql://192.168.1.1:1234";
    actualParsedOptions = translateToPgPoolOptions(uri);

    expectedParsedOptions = new PgPoolOptions()
      .setHost("192.168.1.1")
      .setPort(1234);

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingIpv6Address() {
    uri = "postgresql://[2001:db8::1234]/mydb";
    actualParsedOptions = translateToPgPoolOptions(uri);

    expectedParsedOptions = new PgPoolOptions()
      .setHost("2001:db8::1234")
      .setDatabase("mydb");

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingPort() {
    uri = "postgresql://localhost:1234";
    actualParsedOptions = translateToPgPoolOptions(uri);

    expectedParsedOptions = new PgPoolOptions()
      .setHost("localhost")
      .setPort(1234);

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }


  @Test
  public void testParsingDbName() {
    uri = "postgres://localhost/mydb";
    actualParsedOptions = translateToPgPoolOptions(uri);

    expectedParsedOptions = new PgPoolOptions()
      .setHost("localhost")
      .setDatabase("mydb");

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }


  @Test
  public void testParsingParameter() {
    uri = "postgresql://localhost/otherdb?user=other";
    actualParsedOptions = translateToPgPoolOptions(uri);

    expectedParsedOptions = new PgPoolOptions()
      .setUsername("other")
      .setHost("localhost")
      .setDatabase("otherdb");

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingParameters() {
    uri = "postgresql://localhost/otherdb?user=other&password=secret&port=1234";
    actualParsedOptions = translateToPgPoolOptions(uri);

    expectedParsedOptions = new PgPoolOptions()
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
    actualParsedOptions = translateToPgPoolOptions(uri);

    expectedParsedOptions = new PgPoolOptions()
      .setHost("localhost")
      .setUsername("other")
      .setPassword("secret");

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingUserWithoutPassword() {
    uri = "postgresql://user@";
    actualParsedOptions = translateToPgPoolOptions(uri);

    expectedParsedOptions = new PgPoolOptions()
      .setUsername("user");

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingPasswordWithoutUsername() {
    uri = "postgresql://:secret@";
    actualParsedOptions = translateToPgPoolOptions(uri);

    expectedParsedOptions = new PgPoolOptions();

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingPortWithoutHost() {
    // This URI is not valid in java.net.URI
    uri = "postgresql://:1234";
    actualParsedOptions = translateToPgPoolOptions(uri);

    expectedParsedOptions = new PgPoolOptions()
      .setPort(1234);

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingOnlyDbName() {
    uri = "postgresql:///mydb";
    actualParsedOptions = translateToPgPoolOptions(uri);

    expectedParsedOptions = new PgPoolOptions()
      .setDatabase("mydb");

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingOnlyParameters() {
    uri = "postgresql://?host=localhost&port=1234";
    actualParsedOptions = translateToPgPoolOptions(uri);

    expectedParsedOptions = new PgPoolOptions()
      .setHost("localhost")
      .setPort(1234);

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingDomainSocket() {
    uri = "postgresql://%2Fvar%2Flib%2Fpostgresql/dbname";
    actualParsedOptions = translateToPgPoolOptions(uri);

    expectedParsedOptions = new PgPoolOptions()
      .setDatabase("dbname");

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingDomainSocketInParameter() {
    uri = "postgresql:///dbname?host=/var/lib/postgresql";
    actualParsedOptions = translateToPgPoolOptions(uri);

    expectedParsedOptions = new PgPoolOptions()
      .setDatabase("dbname");

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingInvalidUri() {
    uri = "postgresql://@@/dbname?host";
    actualParsedOptions = translateToPgPoolOptions(uri);

    expectedParsedOptions = new PgPoolOptions();

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingUriWithOverridenParameters() {
    uri = "postgresql://localhost/mydb?host=myhost&port=1234";
    actualParsedOptions = translateToPgPoolOptions(uri);

    expectedParsedOptions = new PgPoolOptions()
      .setHost("myhost")
      .setDatabase("mydb")
      .setPort(1234);

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testParsingFullUri() {
    uri = "postgresql://dbuser:secretpassword@database.server.com:3211/mydb";
    actualParsedOptions = translateToPgPoolOptions(uri);

    expectedParsedOptions = new PgPoolOptions()
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

    PgConnectOptions actualPgConnectOptions = PgConnectionUriParser.translateToPgConnectOptions(uri);

    assertEquals(expectedPgConnectOptions, actualPgConnectOptions);
  }

  @Test
  public void testConvertingPgConnectOptions() {
    uri = "postgresql://pg@localhost?password=secret123&port=1234";

    PgConnectOptions expectedOptions = new PgConnectOptions()
      .setUsername("pg")
      .setPassword("secret123")
      .setHost("localhost")
      .setPort(1234);

    assertEquals(expectedOptions, translateToPgConnectOptions(uri));
  }

  @Test
  public void testConvertingWrongPgConnectOptions() {
    uri = "postgresql://user:secret@localhost/mydb?port=1234";

    PgConnectOptions wrongOptions = new PgConnectOptions()
      .setUsername("pg")
      .setPassword("secret123")
      .setHost("localhost")
      .setPort(1234);

    assertNotEquals(wrongOptions, translateToPgConnectOptions(uri));
  }

  @Test
  public void testInvalidUri1() {
    uri = "postgresql://us@er@@";
    actualParsedOptions = translateToPgPoolOptions(uri);

    expectedParsedOptions = new PgPoolOptions();

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testInvalidUri2() {
    uri = "postgresql://user/mydb//";
    actualParsedOptions = translateToPgPoolOptions(uri);

    expectedParsedOptions = new PgPoolOptions();

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testInvalidUri3() {
    uri = "postgresql:///dbname/?host=localhost";
    actualParsedOptions = translateToPgPoolOptions(uri);

    expectedParsedOptions = new PgPoolOptions();

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testInvalidUri4() {
    uri = "postgresql://user::1234";
    actualParsedOptions = translateToPgPoolOptions(uri);

    expectedParsedOptions = new PgPoolOptions();

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testInvalidUri5() {
    uri = "postgresql://@:1234";
    actualParsedOptions = translateToPgPoolOptions(uri);

    expectedParsedOptions = new PgPoolOptions();

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }

  @Test
  public void testInvalidUri6() {
    uri = "postgresql://:123:";
    actualParsedOptions = translateToPgPoolOptions(uri);

    expectedParsedOptions = new PgPoolOptions();

    assertEquals(expectedParsedOptions, actualParsedOptions);
  }
}

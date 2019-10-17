package io.vertx.mysqlclient;

import org.junit.Assert;
import org.junit.Test;

public class MySQLConnectOptionsProviderTest {
  private String connectionUri;
  private MySQLConnectOptions expectedConfiguration;
  private MySQLConnectOptions actualConfiguration;

  @Test
  public void testValidUri1() {
    connectionUri = "mysql://localhost";
    actualConfiguration = MySQLConnectOptions.fromUri(connectionUri);

    expectedConfiguration = new MySQLConnectOptions();

    assertEquals(expectedConfiguration, actualConfiguration);
  }

  @Test
  public void testValidUri2() {
    connectionUri = "mysql://myhost";
    actualConfiguration = MySQLConnectOptions.fromUri(connectionUri);

    expectedConfiguration = new MySQLConnectOptions()
      .setHost("myhost");

    assertEquals(expectedConfiguration, actualConfiguration);
  }

  @Test
  public void testValidUri3() {
    connectionUri = "mysql://myhost:3306";
    actualConfiguration = MySQLConnectOptions.fromUri(connectionUri);

    expectedConfiguration = new MySQLConnectOptions()
      .setHost("myhost")
      .setPort(3306);

    assertEquals(expectedConfiguration, actualConfiguration);
  }

  @Test
  public void testValidUri4() {
    connectionUri = "mysql://myhost/mydb";
    actualConfiguration = MySQLConnectOptions.fromUri(connectionUri);

    expectedConfiguration = new MySQLConnectOptions()
      .setHost("myhost")
      .setDatabase("mydb");

    assertEquals(expectedConfiguration, actualConfiguration);
  }

  @Test
  public void testValidUri5() {
    connectionUri = "mysql://user@myhost";
    actualConfiguration = MySQLConnectOptions.fromUri(connectionUri);

    expectedConfiguration = new MySQLConnectOptions()
      .setUser("user")
      .setHost("myhost");

    assertEquals(expectedConfiguration, actualConfiguration);
  }

  @Test
  public void testValidUri6() {
    connectionUri = "mysql://user:secret@myhost";
    actualConfiguration = MySQLConnectOptions.fromUri(connectionUri);

    expectedConfiguration = new MySQLConnectOptions()
      .setUser("user")
      .setPassword("secret")
      .setHost("myhost");

    assertEquals(expectedConfiguration, actualConfiguration);
  }

  @Test
  public void testValidUri7() {
    connectionUri = "mysql://other@localhost/otherdb?port=3306&password=secret";
    actualConfiguration = MySQLConnectOptions.fromUri(connectionUri);

    expectedConfiguration = new MySQLConnectOptions()
      .setUser("other")
      .setPassword("secret")
      .setHost("localhost")
      .setPort(3306)
      .setDatabase("otherdb");

    assertEquals(expectedConfiguration, actualConfiguration);
  }

  @Test
  public void testValidUri8() {
    connectionUri = "mariadb://other@localhost/otherdb?port=3306&password=secret";
    actualConfiguration = MySQLConnectOptions.fromUri(connectionUri);

    expectedConfiguration = new MySQLConnectOptions()
      .setUser("other")
      .setPassword("secret")
      .setHost("localhost")
      .setPort(3306)
      .setDatabase("otherdb");

    assertEquals(expectedConfiguration, actualConfiguration);
  }

  @Test
  public void testValidUri9() {
    connectionUri = "mysql://myhost?useAffectedRows=true";
    actualConfiguration = MySQLConnectOptions.fromUri(connectionUri);

    expectedConfiguration = new MySQLConnectOptions()
      .setHost("myhost")
      .setUseAffectedRows(true);

    assertEquals(expectedConfiguration, actualConfiguration);
  }

  @Test
  public void testValidUri10() {
    connectionUri = "mysql://myhost?useAffectedRows=all_except_true_is_false";
    actualConfiguration = MySQLConnectOptions.fromUri(connectionUri);

    expectedConfiguration = new MySQLConnectOptions()
      .setHost("myhost")
      .setUseAffectedRows(false);

    assertEquals(expectedConfiguration, actualConfiguration);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUri1() {
    connectionUri = "mysql://username:password@loc//dbname";
    actualConfiguration = MySQLConnectOptions.fromUri(connectionUri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUri2() {
    connectionUri = "mysql://user@:passowrd@localhost/dbname/qwer";
    actualConfiguration = MySQLConnectOptions.fromUri(connectionUri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUri3() {
    connectionUri = "mysql://user:password@localhost:655355/dbname";
    actualConfiguration = MySQLConnectOptions.fromUri(connectionUri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUri4() {
    connectionUri = "mysql://user@localhost?port=1234&port";
    actualConfiguration = MySQLConnectOptions.fromUri(connectionUri);
  }

  private static void assertEquals(MySQLConnectOptions expectedConfiguration, MySQLConnectOptions actualConfiguration) {
    Assert.assertEquals(expectedConfiguration.toJson(), actualConfiguration.toJson());
  }
}

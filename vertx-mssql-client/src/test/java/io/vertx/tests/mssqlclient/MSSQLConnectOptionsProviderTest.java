package io.vertx.tests.mssqlclient;/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

import io.vertx.mssqlclient.MSSQLConnectOptions;
import org.junit.Assert;
import org.junit.Test;

public class MSSQLConnectOptionsProviderTest {
  private String connectionUri;
  private MSSQLConnectOptions expectedConfiguration;
  private MSSQLConnectOptions actualConfiguration;

  @Test
  public void testValidUri1() {
    connectionUri = "sqlserver://localhost";
    actualConfiguration = MSSQLConnectOptions.fromUri(connectionUri);

    expectedConfiguration = new MSSQLConnectOptions();

    assertEquals(expectedConfiguration, actualConfiguration);
  }

  @Test
  public void testValidUri2() {
    connectionUri = "sqlserver://myhost";
    actualConfiguration = MSSQLConnectOptions.fromUri(connectionUri);

    expectedConfiguration = new MSSQLConnectOptions()
      .setHost("myhost");

    assertEquals(expectedConfiguration, actualConfiguration);
  }

  @Test
  public void testValidUri3() {
    connectionUri = "sqlserver://myhost:3306";
    actualConfiguration = MSSQLConnectOptions.fromUri(connectionUri);

    expectedConfiguration = new MSSQLConnectOptions()
      .setHost("myhost")
      .setPort(3306);

    assertEquals(expectedConfiguration, actualConfiguration);
  }

  @Test
  public void testValidUri4() {
    connectionUri = "sqlserver://myhost/mydb";
    actualConfiguration = MSSQLConnectOptions.fromUri(connectionUri);

    expectedConfiguration = new MSSQLConnectOptions()
      .setHost("myhost")
      .setDatabase("mydb");

    assertEquals(expectedConfiguration, actualConfiguration);
  }

  @Test
  public void testValidUri5() {
    connectionUri = "sqlserver://user@myhost";
    actualConfiguration = MSSQLConnectOptions.fromUri(connectionUri);

    expectedConfiguration = new MSSQLConnectOptions()
      .setUser("user")
      .setHost("myhost");

    assertEquals(expectedConfiguration, actualConfiguration);
  }

  @Test
  public void testValidUri6() {
    connectionUri = "sqlserver://user:secret@myhost";
    actualConfiguration = MSSQLConnectOptions.fromUri(connectionUri);

    expectedConfiguration = new MSSQLConnectOptions()
      .setUser("user")
      .setPassword("secret")
      .setHost("myhost");

    assertEquals(expectedConfiguration, actualConfiguration);
  }

  @Test
  public void testValidUri7() {
    connectionUri = "sqlserver://other@localhost/otherdb?port=3306&password=secret";
    actualConfiguration = MSSQLConnectOptions.fromUri(connectionUri);

    expectedConfiguration = new MSSQLConnectOptions()
      .setUser("other")
      .setPassword("secret")
      .setHost("localhost")
      .setPort(3306)
      .setDatabase("otherdb");

    assertEquals(expectedConfiguration, actualConfiguration);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUri1() {
    connectionUri = "sqlserver://username:password@loc//dbname";
    actualConfiguration = MSSQLConnectOptions.fromUri(connectionUri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUri2() {
    connectionUri = "sqlserver://user@:passowrd@localhost/dbname/qwer";
    actualConfiguration = MSSQLConnectOptions.fromUri(connectionUri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUri3() {
    connectionUri = "sqlserver://user:password@localhost:655355/dbname";
    actualConfiguration = MSSQLConnectOptions.fromUri(connectionUri);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUri4() {
    connectionUri = "sqlserver://user@localhost?port=1234&port";
    actualConfiguration = MSSQLConnectOptions.fromUri(connectionUri);
  }

  private static void assertEquals(MSSQLConnectOptions expectedConfiguration, MSSQLConnectOptions actualConfiguration) {
    Assert.assertEquals(expectedConfiguration.toJson(), actualConfiguration.toJson());
  }
}

/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package tests.oracleclient.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.oracleclient.impl.OracleConnectionUriParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.function.BiConsumer;

import static java.util.Locale.ENGLISH;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class InvalidOracleConnectionUriParsingTest {

  @Parameters(name = "{0}: {1}")
  public static Object[][] testData() {
    Object[][] params = {
      testData("null uri", null),
      testData("uri with invalid scheme", "postgresql://user@?host=localhost&port=1234"),
      testData("uri with no separated user/password", "oracle:thin:scott@myhost:1521:orcl"),
      testData("uri with no separated user/password", "oracle:thin:scott@myhost:1521:orcl"),
      testData("uri without password", "oracle:thin:scott/@myhost:1521:orcl"),
      testData("uri without user", "oracle:thin:/tiger@myhost:1521:orcl"),
      testData("uri with multiple user/password splitters", "oracle:thin:scott/tiger/dragon@myhost:1521:orcl"),
      testData("uri without net location", "oracle:thin:scott/tiger"),
      testData("uri without SID after host", "oracle:thin:scott/tiger@myhost"),
      testData("uri without SID after port", "oracle:thin:scott/tiger@myhost:1521"),
      testData("uri with empty SID", "oracle:thin:scott/tiger@myhost:1521:"),
      testData("uri with invalid content after host", "oracle:thin:@[::1]sss:1521:orcl"),
      testData("uri with invalid IPv6 address", "oracle:thin:@[:1521:orcl"),
      testData("uri with empty host", "oracle:thin:@:1521:orcl"),
      testData("uri with empty IPv6 address", "oracle:thin:@[]:1521:orcl"),
      testData("uri with empty port", "oracle:thin:@myhost::orcl"),
      testData("uri with invalid port", "oracle:thin:@myhost:7654645:orcl"),
      testData("uri with multiple hosts and ports", "oracle:thin:scott/tiger@myhost1:1521,myhost2:1521:orcl"),
      testData("uri with multiple hosts", "oracle:thin:scott/tiger@myhost1,myhost2:1521:orcl"),
      testData("uri with empty props", "oracle:thin:scott/tiger@myhost:1521:orcl?"),
      testData("uri with empty service name", "oracle:thin:scott/tiger@myhost:1521/"),
      testData("uri with empty server mode", "oracle:thin:scott/tiger@myhost:1521/orcl:?prop=val"),
      testData("uri with invalid server mode", "oracle:thin:scott/tiger@myhost:1521/orcl:foo"),
      testData("uri with service name but empty instance name", "oracle:thin:scott/tiger@myhost:1521/orcl/"),
      testData("uri with service name and server mode but empty instance name", "oracle:thin:scott/tiger@myhost:1521/orcl:shared/"),
      testData("uri with empty prop", "oracle:thin:scott/tiger@myhost:1521:orcl?&prop2"),
      testData("uri with prop having no value", "oracle:thin:scott/tiger@myhost:1521:orcl?prop1&prop2=val2"),
      testData(
        "uri with ldap syntax", "oracle:thin:@ldap://ldap.acme.com:7777/sales,cn=OracleContext,dc=com",
        (s, e) -> {
          assertNotNull(e.getCause());
          assertTrue(e.getCause().getMessage().toLowerCase(ENGLISH).contains("ldap"));
        }),
      testData("uri with Oracle Net connection descriptor", "oracle:thin:@(DESCRIPTION=\n" +
          "  (LOAD_BALANCE=on)\n" +
          "(ADDRESS_LIST=\n" +
          "  (ADDRESS=(PROTOCOL=TCP)(HOST=host1) (PORT=1521))\n" +
          " (ADDRESS=(PROTOCOL=TCP)(HOST=host2)(PORT=1521)))\n" +
          " (CONNECT_DATA=(SERVICE_NAME=service_name)))",
        (s, e) -> {
          assertNotNull(e.getCause());
          assertTrue(e.getCause().getMessage().toLowerCase(ENGLISH).contains("tns url"));
        }),
      testData("uri with empty TNSNames alias", "oracle:thin:@?key=val"),
    };
    return params;
  }

  private static Object[] testData(String testName, String uri) {
    return testData(testName, uri, null);
  }

  private static Object[] testData(String testName, String uri, BiConsumer<String, Exception> assertions) {
    return new Object[]{testName, uri, assertions};
  }

  private final String connectionUri;
  private final BiConsumer<String, Exception> assertions;

  public InvalidOracleConnectionUriParsingTest(@SuppressWarnings("unused") String name, String connectionUri, BiConsumer<String, Exception> assertions) {
    this.connectionUri = connectionUri;
    this.assertions = assertions;
  }

  @Test
  public void shouldFailToParseInvalidUri() {
    try {
      JsonObject conf = OracleConnectionUriParser.parse(connectionUri);
      fail(String.format("Should fail to parse: %s\n%s", connectionUri, conf.encodePrettily()));
    } catch (Exception e) {
      if (connectionUri == null) {
        assertThat(e, is(instanceOf(NullPointerException.class)));
      } else {
        assertThat(e, is(instanceOf(IllegalArgumentException.class)));
      }
      if (assertions != null) {
        assertions.accept(connectionUri, e);
      }
    }
  }
}

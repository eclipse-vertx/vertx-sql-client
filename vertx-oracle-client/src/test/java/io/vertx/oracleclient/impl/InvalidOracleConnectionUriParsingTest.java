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

package io.vertx.oracleclient.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class InvalidOracleConnectionUriParsingTest {

  @Parameters(name = "{0}: {1}")
  public static Object[][] data() {
    Object[][] params = {
      {"null uri", null},
      {"uri with invalid scheme", "postgresql://user@?host=localhost&port=1234"},
      {"uri with no separated user/password", "oracle:thin:scott@myhost:1521:orcl"},
      {"uri with no separated user/password", "oracle:thin:scott@myhost:1521:orcl"},
      {"uri without password", "oracle:thin:scott/@myhost:1521:orcl"},
      {"uri without user", "oracle:thin:/tiger@myhost:1521:orcl"},
      {"uri with multiple user/password splitters", "oracle:thin:scott/tiger/dragon@myhost:1521:orcl"},
      {"uri without net location", "oracle:thin:scott/tiger"},
      {"uri without SID after host", "oracle:thin:scott/tiger@myhost"},
      {"uri without SID after port", "oracle:thin:scott/tiger@myhost:1521"},
      {"uri with empty SID", "oracle:thin:scott/tiger@myhost:1521:"},
      {"uri with invalid content after host", "oracle:thin:@[::1]sss:1521:orcl"},
      {"uri with invalid IPv6 address", "oracle:thin:@[:1521:orcl"},
      {"uri with empty host", "oracle:thin:@:1521:orcl"},
      {"uri with empty IPv6 address", "oracle:thin:@[]:1521:orcl"},
      {"uri with empty port", "oracle:thin:@myhost::orcl"},
      {"uri with invalid port", "oracle:thin:@myhost:7654645:orcl"},
      {"uri with multiple hosts and ports", "oracle:thin:scott/tiger@myhost1:1521,myhost2:1521:orcl"},
      {"uri with multiple hosts", "oracle:thin:scott/tiger@myhost1,myhost2:1521:orcl"},
      {"uri with empty props", "oracle:thin:scott/tiger@myhost:1521:orcl?"},
      {"uri with empty service name", "oracle:thin:scott/tiger@myhost:1521/"},
      {"uri with empty server mode", "oracle:thin:scott/tiger@myhost:1521/orcl:?prop=val"},
      {"uri with invalid server mode", "oracle:thin:scott/tiger@myhost:1521/orcl:foo"},
      {"uri with service name but empty instance name", "oracle:thin:scott/tiger@myhost:1521/orcl/"},
      {"uri with service name and server mode but empty instance name", "oracle:thin:scott/tiger@myhost:1521/orcl:shared/"},
      {"uri with empty prop", "oracle:thin:scott/tiger@myhost:1521:orcl?&prop2"},
      {"uri with prop having no value", "oracle:thin:scott/tiger@myhost:1521:orcl?prop1&prop2=val2"},
    };
    return params;
  }

  private final String connectionUri;

  public InvalidOracleConnectionUriParsingTest(@SuppressWarnings("unused") String name, String connectionUri) {
    this.connectionUri = connectionUri;
  }

  @Test
  public void shouldFailToParseInvalidUri() {
    try {
      OracleConnectionUriParser.parse(connectionUri);
      fail("Should fail to parse: " + connectionUri);
    } catch (Exception e) {
      if (connectionUri == null) {
        assertThat(e, is(instanceOf(NullPointerException.class)));
      } else {
        assertThat(e, is(instanceOf(IllegalArgumentException.class)));
      }
    }
  }
}

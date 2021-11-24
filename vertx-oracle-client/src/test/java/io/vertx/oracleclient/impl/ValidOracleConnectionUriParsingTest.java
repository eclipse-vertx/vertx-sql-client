/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.oracleclient.impl;

import io.vertx.core.json.JsonObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class ValidOracleConnectionUriParsingTest {

  @Parameters(name = "{0}: {1}")
  public static Object[][] data() {
    Object[][] params = {
      {"uri with user and password", "oracle:thin:scott/tiger@myhost:1521:orcl",
        new JsonObject()
          .put("database", "orcl")
          .put("host", "myhost")
          .put("port", 1521)
          .put("user", "scott")
          .put("password", "tiger")},
      {"uri without user and password", "oracle:thin:@myhost:1521:orcl",
        new JsonObject()
          .put("database", "orcl")
          .put("host", "myhost")
          .put("port", 1521)},
    };
    return params;
  }

  private final String connectionUri;
  private final JsonObject expected;

  public ValidOracleConnectionUriParsingTest(@SuppressWarnings("unused") String name, String connectionUri, JsonObject expected) {
    this.connectionUri = connectionUri;
    this.expected = expected;
  }

  @Test
  public void shouldParseValidUri() {
    assertEquals(expected, OracleConnectionUriParser.parse(connectionUri));
  }
}

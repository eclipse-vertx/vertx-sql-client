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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class InvalidOracleConnectionUriParsingTest {

  @Parameters(name = "{0}: {1}")
  public static Object[][] data() {
    Object[][] params = {
      {"uri with no separated user/password", "oracle:thin:scott@myhost:1521:orcl"},
      {"uri without password", "oracle:thin:scott/@myhost:1521:orcl"},
      {"uri without user", "oracle:thin:/tiger@myhost:1521:orcl"},
      {"uri with multiple user/password splitters", "oracle:thin:scott/tiger/dragon@myhost:1521:orcl"},
      {"uri without net location", "oracle:thin:scott/tiger"},
      {"uri without SID", "oracle:thin:scott/tiger@myhost:1521"},
    };
    return params;
  }

  private final String connectionUri;

  public InvalidOracleConnectionUriParsingTest(@SuppressWarnings("unused") String name, String connectionUri) {
    this.connectionUri = connectionUri;
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailToParseInvalidUri() {
    OracleConnectionUriParser.parse(connectionUri);
  }
}

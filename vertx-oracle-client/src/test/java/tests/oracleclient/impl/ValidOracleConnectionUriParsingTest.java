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
import org.junit.Assert;
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
      {"uri with user and password and sid", "oracle:thin:scott/tiger@myhost:1521:orcl",
        new JsonObject()
          .put("serviceId", "orcl")
          .put("host", "myhost")
          .put("port", 1521)
          .put("user", "scott")
          .put("password", "tiger")},
      {"uri with sid but without port", "oracle:thin:scott/tiger@myhost:orcl",
        new JsonObject()
          .put("serviceId", "orcl")
          .put("host", "myhost")
          .put("user", "scott")
          .put("password", "tiger")},
      {"uri without user and password", "oracle:thin:@myhost:1521:orcl",
        new JsonObject()
          .put("serviceId", "orcl")
          .put("host", "myhost")
          .put("port", 1521)},
      {"uri with tcp protocol", "oracle:thin:@tcp://myhost:1521:orcl",
        new JsonObject()
          .put("serviceId", "orcl")
          .put("host", "myhost")
          .put("port", 1521)},
      {"uri with tcps protocol", "oracle:thin:@tcps://myhost:1521:orcl",
        new JsonObject()
          .put("ssl", true)
          .put("serviceId", "orcl")
          .put("host", "myhost")
          .put("port", 1521)},
      {"uri with one connection property", "oracle:thin:@myhost:1521:orcl?key=val",
        new JsonObject()
          .put("properties", new JsonObject().put("key", "val"))
          .put("serviceId", "orcl")
          .put("host", "myhost")
          .put("port", 1521)},
      {"uri with several connection properties", "oracle:thin:@myhost:1521:orcl?k1=v1&k2=v2&k3=v3",
        new JsonObject()
          .put("properties", new JsonObject().put("k1", "v1").put("k2", "v2").put("k3", "v3"))
          .put("serviceId", "orcl")
          .put("host", "myhost")
          .put("port", 1521)},
      {"uri with service name", "oracle:thin:@[::1]:1521/orcl",
        new JsonObject()
          .put("serviceName", "orcl")
          .put("host", "::1")
          .put("port", 1521)},
      {"uri with service name and instance name", "oracle:thin:@[::1]:1521/orcl/xe",
        new JsonObject()
          .put("serviceName", "orcl")
          .put("instanceName", "xe")
          .put("host", "::1")
          .put("port", 1521)},
      {"uri with service name, server mode and instance name", "oracle:thin:@[::1]/orcl:shared/xe",
        new JsonObject()
          .put("serviceName", "orcl")
          .put("instanceName", "xe")
          .put("host", "::1")
          .put("serverMode", "shared")},
      {"uri with service name and server mode", "oracle:thin:@[::1]/orcl:dedicated",
        new JsonObject()
          .put("serviceName", "orcl")
          .put("host", "::1")
          .put("serverMode", "dedicated")},
      {"uri with service name and server mode pooled", "oracle:thin:@[::1]/orcl:pooled",
        new JsonObject()
          .put("serviceName", "orcl")
          .put("host", "::1")
          .put("serverMode", "pooled")},
      {"uri with service name with prop", "oracle:thin:@[::1]:1521/orcl?key=val",
        new JsonObject()
          .put("properties", new JsonObject().put("key", "val"))
          .put("serviceName", "orcl")
          .put("host", "::1")
          .put("port", 1521)},
      {"uri with service name and instance name with prop", "oracle:thin:@[::1]:1521/orcl/xe?key=val",
        new JsonObject()
          .put("properties", new JsonObject().put("key", "val"))
          .put("serviceName", "orcl")
          .put("instanceName", "xe")
          .put("host", "::1")
          .put("port", 1521)},
      {"uri with service name, server mode and instance name with prop", "oracle:thin:@[::1]/orcl:shared/xe?key=val",
        new JsonObject()
          .put("properties", new JsonObject().put("key", "val"))
          .put("serviceName", "orcl")
          .put("instanceName", "xe")
          .put("host", "::1")
          .put("serverMode", "shared")},
      {"uri with service name and server mode with prop", "oracle:thin:@[::1]/orcl:dedicated?key=val",
        new JsonObject()
          .put("properties", new JsonObject().put("key", "val"))
          .put("serviceName", "orcl")
          .put("host", "::1")
          .put("serverMode", "dedicated")},
      {"uri with TNSNames alias and TNS ADMIN short prop", "oracle:thin:@prod_db?TNS_ADMIN=/work/tns",
        new JsonObject()
          .put("tnsAlias", "prod_db")
          .put("tnsAdmin", "/work/tns")},
      {"uri with TNSNames alias", "oracle:thin:@prod_db",
        new JsonObject()
          .put("tnsAlias", "prod_db")},
      {"uri with TNSNames alias with prop", "oracle:thin:@prod_db?key=val",
        new JsonObject()
          .put("properties", new JsonObject().put("key", "val"))
          .put("tnsAlias", "prod_db")},
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
    Assert.assertEquals(expected, OracleConnectionUriParser.parse(connectionUri));
  }
}

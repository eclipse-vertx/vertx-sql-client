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

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.oracleclient.impl.OracleConnectionUriParser;
import io.vertx.oracleclient.impl.OracleDatabaseHelper;
import oracle.jdbc.datasource.OracleDataSource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.sql.SQLException;


@RunWith(Parameterized.class)
public class ValidOracleConnectionUriParsingTest {

  @Parameters(name = "{0}: {1}")
  public static Object[][] data() {
    Object[][] params = {
      {"uri with user and password and sid", "oracle:thin:scott/tiger@myhost:1521:orcl",
        // expected generated Easy Connect Plus connection string from OracleDatabaseHelper.composeJdbcUrl
        // without user/password and properties (it is retrieved from the OracleDataSource.getURL because
        // OracleDatabaseHelper.composeJdbcUrl method access is private
        "jdbc:oracle:thin:@myhost:1521:orcl",
        new JsonObject()
          .put("serviceId", "orcl")
          .put("host", "myhost")
          .put("port", 1521)
          .put("user", "scott")
          .put("password", "tiger")},
      {"uri with sid but without port", "oracle:thin:scott/tiger@myhost:orcl",
        "jdbc:oracle:thin:@myhost:1521:orcl",
        new JsonObject()
          .put("serviceId", "orcl")
          .put("host", "myhost")
          .put("user", "scott")
          .put("password", "tiger")},
      {"uri without user and password", "oracle:thin:@myhost:1521:orcl",
        "jdbc:oracle:thin:@myhost:1521:orcl",
        new JsonObject()
          .put("serviceId", "orcl")
          .put("host", "myhost")
          .put("port", 1521)},
      {"uri with tcp protocol", "oracle:thin:@tcp://myhost:1521:orcl",
        "jdbc:oracle:thin:@myhost:1521:orcl",
        new JsonObject()
          .put("serviceId", "orcl")
          .put("host", "myhost")
          .put("port", 1521)},
      {"uri with tcps protocol", "oracle:thin:@tcps://myhost:1521:orcl",
        "jdbc:oracle:thin:@tcps://myhost:1521:orcl",
        new JsonObject()
          .put("ssl", true)
          .put("serviceId", "orcl")
          .put("host", "myhost")
          .put("port", 1521)},
      {"uri with one connection property", "oracle:thin:@myhost:1521:orcl?key=val",
        "jdbc:oracle:thin:@myhost:1521:orcl",
        new JsonObject()
          .put("properties", new JsonObject().put("key", "val"))
          .put("serviceId", "orcl")
          .put("host", "myhost")
          .put("port", 1521)},
      {"uri with several connection properties", "oracle:thin:@myhost:1521:orcl?k1=v1&k2=v2&k3=v3",
        "jdbc:oracle:thin:@myhost:1521:orcl",
        new JsonObject()
          .put("properties", new JsonObject().put("k1", "v1").put("k2", "v2").put("k3", "v3"))
          .put("serviceId", "orcl")
          .put("host", "myhost")
          .put("port", 1521)},
      {"uri with service name", "oracle:thin:@localhost/freepdb1",
        "jdbc:oracle:thin:@localhost:1521/freepdb1",
        new JsonObject()
          .put("serviceName", "freepdb1")
          .put("host", "localhost")},
      {"uri with service name and non default port", "oracle:thin:@localhost:1522/freepdb1",
        "jdbc:oracle:thin:@localhost:1522/freepdb1",
        new JsonObject()
          .put("serviceName", "freepdb1")
          .put("host", "localhost")
          .put("port", 1522)},
      {"uri with service name and port and default port", "oracle:thin:@//localhost:1521/freepdb1",
        "jdbc:oracle:thin:@localhost:1521/freepdb1",
        new JsonObject()
          .put("serviceName", "freepdb1")
          .put("host", "localhost")
          .put("port", 1521)},
      {"uri with service name and port and default port", "oracle:thin:@tcps://127.0.0.1:1522/freepdb1",
        "jdbc:oracle:thin:@tcps://127.0.0.1:1522/freepdb1",
        new JsonObject()
          .put("serviceName", "freepdb1")
          .put("ssl", true)
          .put("host", "127.0.0.1")
          .put("port", 1522)},
      {"uri with service name and port and default port", "oracle:thin:@tcps://[2001:0db8:0:0::200C:417A]:1523/freepdb1",
        "jdbc:oracle:thin:@tcps://[2001:0db8:0:0::200C:417A]:1523/freepdb1",
        new JsonObject()
          .put("serviceName", "freepdb1")
          .put("ssl", true)
          .put("host", "2001:0db8:0:0::200C:417A")
          .put("port", 1523)},
      {"uri with service name and port and default port", "oracle:thin:@tcps://[2001:0db8:0:0::200C:417A]:1523/freepdb1:DEDICATED/inst1",
        "jdbc:oracle:thin:@tcps://[2001:0db8:0:0::200C:417A]:1523/freepdb1/inst1",
        new JsonObject()
          .put("serviceName", "freepdb1")
          .put("serverMode", "DEDICATED")
          .put("instanceName", "inst1")
          .put("ssl", true)
          .put("host", "2001:0db8:0:0::200C:417A")
          .put("port", 1523)},
      {"uri with service name and port and default port", "oracle:thin:@//[2001:0db8:0:0::200C:417A]:1523/freepdb1:DEDICATED",
        "jdbc:oracle:thin:@[2001:0db8:0:0::200C:417A]:1523/freepdb1",
        new JsonObject()
          .put("serviceName", "freepdb1")
          .put("serverMode", "DEDICATED")
          .put("host", "2001:0db8:0:0::200C:417A")
          .put("port", 1523)},
      {"uri with service name and port and default port", "oracle:thin:@tcp://[2001:0db8:0:0::200C:417A]/freepdb1/inst1",
        "jdbc:oracle:thin:@[2001:0db8:0:0::200C:417A]:1521/freepdb1/inst1",
        new JsonObject()
          .put("serviceName", "freepdb1")
          .put("instanceName", "inst1")
          .put("host", "2001:0db8:0:0::200C:417A")},
      {"uri with service name and IPv6", "oracle:thin:@[::1]:1521/orcl",
        "jdbc:oracle:thin:@[::1]:1521/orcl",
        new JsonObject()
          .put("serviceName", "orcl")
          .put("host", "::1")
          .put("port", 1521)},
      {"uri with service name and instance name", "oracle:thin:@[::1]:1521/orcl/xe",
        "jdbc:oracle:thin:@[::1]:1521/orcl/xe",
        new JsonObject()
          .put("serviceName", "orcl")
          .put("instanceName", "xe")
          .put("host", "::1")
          .put("port", 1521)},
      {"uri with service name, server mode and instance name", "oracle:thin:@[::1]/orcl:SHARED/xe",
        "jdbc:oracle:thin:@[::1]:1521/orcl:shared/xe",
        new JsonObject()
          .put("serviceName", "orcl")
          .put("instanceName", "xe")
          .put("host", "::1")
          .put("serverMode", "SHARED")},
      {"uri with service name and server mode", "oracle:thin:@[::1]/orcl:DEDICATED",
        "jdbc:oracle:thin:@[::1]:1521/orcl",
        new JsonObject()
          .put("serviceName", "orcl")
          .put("host", "::1")
          .put("serverMode", "DEDICATED")},
      {"uri with service name with prop", "oracle:thin:@[::1]:1521/orcl?key=val",
        "jdbc:oracle:thin:@[::1]:1521/orcl",
        new JsonObject()
          .put("properties", new JsonObject().put("key", "val"))
          .put("serviceName", "orcl")
          .put("host", "::1")
          .put("port", 1521)},
      {"uri with service name and instance name with prop", "oracle:thin:@[::1]:1521/orcl/xe?key=val",
        "jdbc:oracle:thin:@[::1]:1521/orcl/xe",
        new JsonObject()
          .put("properties", new JsonObject().put("key", "val"))
          .put("serviceName", "orcl")
          .put("instanceName", "xe")
          .put("host", "::1")
          .put("port", 1521)},
      {"uri with service name, server mode and instance name with prop", "oracle:thin:@[::1]/orcl:SHARED/xe?key=val",
        "jdbc:oracle:thin:@[::1]:1521/orcl:shared/xe",
        new JsonObject()
          .put("properties", new JsonObject().put("key", "val"))
          .put("serviceName", "orcl")
          .put("instanceName", "xe")
          .put("host", "::1")
          .put("serverMode", "SHARED")},
      {"uri with service name and server mode with prop", "oracle:thin:@[::1]/orcl:DEDICATED?key=val",
        "jdbc:oracle:thin:@[::1]:1521/orcl",
        new JsonObject()
          .put("properties", new JsonObject().put("key", "val"))
          .put("serviceName", "orcl")
          .put("host", "::1")
          .put("serverMode", "DEDICATED")},
      {"uri with multiple hosts and ports", "oracle:thin:scott/tiger@myhost1:1521,myhost2:1521/orcl",
        "jdbc:oracle:thin:@myhost1:1521,myhost2:1521/orcl",
        new JsonObject()
          .put("user", "scott")
          .put("password", "tiger")
          .put("serviceName", "orcl")
          .put("host", "myhost1")
          .put("port", 1521)
          .put("addresses",  new JsonArray()
          .add(new JsonObject().put("host","myhost1").put("port",1521))
          .add(new JsonObject().put("host","myhost2").put("port",1521)))},
      {"uri with multiple hosts", "oracle:thin:scott/tiger@myhost1,myhost2:1521/orcl",
        "jdbc:oracle:thin:@myhost1,myhost2:1521/orcl",
        new JsonObject()
          .put("user", "scott")
          .put("password", "tiger")
          .put("serviceName", "orcl")
          .put("host", "myhost1")
          .put("port", 1521)
          .put("addresses",  new JsonArray()
          .add(new JsonObject().put("host","myhost1"))
          .add(new JsonObject().put("host","myhost2").put("port",1521)))},
      {"uri with multiple hosts", "oracle:thin:scott/tiger@host1:1522,host2:1521,host3,host4:1523/service_name?failover=on&retry_count=30&retry_delay=10",
        "jdbc:oracle:thin:@host1:1522,host2:1521,host3,host4:1523/service_name",
        new JsonObject()
          .put("user", "scott")
          .put("password", "tiger")
          .put("serviceName", "service_name")
          .put("host", "host1")
          .put("port", 1522)
          .put("addresses",  new JsonArray()
          .add(new JsonObject().put("host","host1").put("port",1522))
          .add(new JsonObject().put("host","host2").put("port",1521))
          .add(new JsonObject().put("host","host3"))
          .add(new JsonObject().put("host","host4").put("port",1523)))
          .put("properties", new JsonObject().put("failover", "on").put("retry_count", "30").put("retry_delay", "10"))},
      {"uri with TNSNames alias and TNS ADMIN short prop", "oracle:thin:@prod_db?TNS_ADMIN=/work/tns",
        "jdbc:oracle:thin:@prod_db",
        new JsonObject()
          .put("tnsAlias", "prod_db")
          .put("tnsAdmin", "/work/tns")},
      {"uri with TNSNames alias", "oracle:thin:@prod_db",
        "jdbc:oracle:thin:@prod_db",
        new JsonObject()
          .put("tnsAlias", "prod_db")},
      {"uri with TNSNames alias with prop", "oracle:thin:@prod_db?key=val",
        "jdbc:oracle:thin:@prod_db",
        new JsonObject()
          .put("properties", new JsonObject().put("key", "val"))
          .put("tnsAlias", "prod_db")},
      {"uri with TNS descriptor", "oracle:thin:@(DESCRIPTION=\n" +
        "  (LOAD_BALANCE=on)\n" +
        "(ADDRESS_LIST=\n" +
        "  (ADDRESS=(PROTOCOL=TCP)(HOST=host1) (PORT=1521))\n" +
        " (ADDRESS=(PROTOCOL=TCP)(HOST=host2)(PORT=1521)))\n" +
        " (CONNECT_DATA=(SERVICE_NAME=service_name)))",
        "jdbc:oracle:thin:@host1:1521,host2:1521/service_name",
        new JsonObject()
          .put("serviceName", "service_name")
          .put("host", "host1")
          .put("port", 1521)
          .put("addresses", new JsonArray().add(new JsonObject().put("host", "host1").put("port",1521)).add(new JsonObject().put("host", "host2").put("port",1521)) )
          .put("properties", new JsonObject().put("LOAD_BALANCE", "on"))},
      {"uri with basic TNS descriptor and 1 space", "oracle:thin:@(description= (address=(hostname=localhost))(connect_data=(service_name=freepdb1)))",
        "jdbc:oracle:thin:@localhost:1521/freepdb1",
        new JsonObject()
          .put("serviceName", "freepdb1")
          .put("host", "localhost")
          .put("port", 1521)},
      {"uri with TNS descriptor and user + password", "oracle:thin:scott/tiger@(description=(address=(hostname=localhost))(connect_data=(service_name=freepdb1)))",
        "jdbc:oracle:thin:@localhost:1521/freepdb1",
        new JsonObject()
          .put("user", "scott")
          .put("password", "tiger")
          .put("serviceName", "freepdb1")
          .put("host", "localhost")
          .put("port", 1521)},
      {"uri with TNS descriptor and TCPS protocol", "oracle:thin:@(description=(address=(protocol=TCPS)(PORT=1522)(hostname=localhost))(connect_data=(service_name=freepdb1)))",
        "jdbc:oracle:thin:@tcps://localhost:1522/freepdb1",
        new JsonObject()
          .put("serviceName", "freepdb1")
          .put("ssl", true)
          .put("host", "localhost")
          .put("port", 1522)},
      {"uri with TNS descriptor and server + instance name", "oracle:thin:@(description=(address=(protocol=TCPS)(port=1522)(hostname=localhost))(connect_data=(service_name=freepdb1)(instance_name=inst1)(server=SHARED)))",
        "jdbc:oracle:thin:@tcps://localhost:1522/freepdb1:shared/inst1",
        new JsonObject()
          .put("serviceName", "freepdb1")
          .put("serverMode", "SHARED")
          .put("instanceName", "inst1")
          .put("ssl", true)
          .put("host", "localhost")
          .put("port", 1522)},
      {"uri with TNS descriptor and properties", "oracle:thin:@(description=(SDU=8128)(retry_count=3)(connect_timeout=10)(address=(hostname=localhost))(connect_data=(service_name=freepdb1)))",
        "jdbc:oracle:thin:@localhost:1521/freepdb1",
        new JsonObject()
          .put("serviceName", "freepdb1")
          .put("host", "localhost")
          .put("port", 1521)
          .put("properties", new JsonObject().put("SDU", "8128").put("retry_count", "3").put("connect_timeout", "10"))},
    };
    return params;
  }

  private final String connectionUri;
  private final String expectedEasyConnectPlusUri;
  private final JsonObject expectedConfiguration;

  public ValidOracleConnectionUriParsingTest(@SuppressWarnings("unused") String name, String connectionUri, String expectedEasyConnectPlusUri, JsonObject expectedConfiguration) {
    this.connectionUri = connectionUri;
    this.expectedEasyConnectPlusUri = expectedEasyConnectPlusUri;
    this.expectedConfiguration = expectedConfiguration;
  }

  @Test
  public void shouldParseValidUri() {
    final JsonObject options = OracleConnectionUriParser.parse(connectionUri);
    Assert.assertEquals(expectedConfiguration, options);

    // Verify the generated URL
    final OracleDataSource ods = OracleDatabaseHelper.createDataSource(OracleConnectOptions.fromUri(connectionUri));
    try {
      Assert.assertEquals(expectedEasyConnectPlusUri, ods.getURL());
    }
    catch(SQLException sqle) {
      Assert.fail(sqle.getMessage());
    }
  }
}

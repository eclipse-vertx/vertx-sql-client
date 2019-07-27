/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.vertx.mysqlclient.junit;

import com.wix.mysql.EmbeddedMysql;
import com.wix.mysql.ScriptResolver;
import com.wix.mysql.config.Charset;
import com.wix.mysql.config.MysqldConfig;
import com.wix.mysql.config.SchemaConfig;
import com.wix.mysql.distribution.Version;
import io.vertx.mysqlclient.MySQLConnectOptions;
import org.junit.rules.ExternalResource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;

import java.util.HashMap;
import java.util.Map;

public class MySQLRule extends ExternalResource {
  private static final String connectionUri = System.getProperty("connection.uri");

  private static EmbeddedMysql mysql;
  private static GenericContainer mariadb;

  public synchronized static MySQLConnectOptions startMySQL() throws Exception {
    MysqldConfig mysqldConfig = MysqldConfig.aMysqldConfig(com.wix.mysql.distribution.Version.v5_7_latest)
      .withCharset(Charset.UTF8MB4)
      .withUser("mysql", "password")
      .withPort(3306)
      .withServerVariable("max_allowed_packet", 32 * 1024 * 1024)
      .withServerVariable("max_prepared_stmt_count", 16382)
      .withServerVariable("local_infile", true)
      .build();

    SchemaConfig schemaConfig = SchemaConfig.aSchemaConfig("testschema")
      .withCharset(Charset.UTF8MB4)
      .withScripts(ScriptResolver.classPathScripts("init.sql"))
      .build();

    mysql = EmbeddedMysql.anEmbeddedMysql(mysqldConfig)
      .addSchema(schemaConfig)
      .start();

    return new MySQLConnectOptions()
      .setHost("localhost")
      .setPort(mysqldConfig.getPort())
      .setUser(mysqldConfig.getUsername())
      .setPassword(mysqldConfig.getPassword())
      .setDatabase(schemaConfig.getName())
      .setCollation(mysqldConfig.getCharset().getCollate());
  }

  public synchronized static void stopMySQL() throws Exception {
    if (mysql != null) {
      try {
        mysql.stop();
      } finally {
        mysql = null;
      }
    }
  }

  private static final Map<String, Version> supportedMySQLVersions = new HashMap<>();

  static {
    supportedMySQLVersions.put("5.6", Version.v5_6_latest);
    supportedMySQLVersions.put("5.7", Version.v5_7_latest);
  }

  private static Version getMySQLVersion() {
    String specifiedVersion = System.getProperty("embedded.database.version");
    Version version;
    if (specifiedVersion == null || specifiedVersion.isEmpty()) {
      version = Version.v5_7_latest;
    } else {
      version = supportedMySQLVersions.get(specifiedVersion);
    }
    if (version == null) {
      throw new IllegalArgumentException("embedded MySQL only supports the following versions: " + supportedMySQLVersions.keySet().toString() + "instead of " + specifiedVersion);
    }
    return version;
  }

  public boolean isUsingMariaDB() {
    return mariadb != null;
  }

  public boolean isUsingMySQL5_6() {
    return mysql != null && MySQLRule.getMySQLVersion() == Version.v5_6_latest;
  }

  private MySQLConnectOptions options;
  private Server database;

  public MySQLConnectOptions options() {
    return new MySQLConnectOptions(options);
  }

  @Override
  protected void before() throws Throwable {
    // use an external database for testing
    if (connectionUri != null && !connectionUri.isEmpty()) {
      options = MySQLConnectOptions.fromUri(connectionUri);
      return;
    }

    String databaseInfoString = System.getProperty("embedded.database.server");
    if (databaseInfoString != null && !databaseInfoString.isEmpty()) {
      database = parseDatabase(databaseInfoString);
    } else {
      database = Server.MySQL;
    }
    if (database == Server.MySQL) {
      options = startMySQL();
    } else if (database == Server.MariaDB) {
      options = startMariaDB();
    }
  }

  @Override
  protected void after() {
    if (options != null) {
      try {
        if (database == Server.MySQL) {
          stopMySQL();
        } else if (database == Server.MariaDB) {
          stopMariaDB();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private enum Server {
    MySQL, MariaDB
  }

  private static Server parseDatabase(String databaseInfo) throws IllegalArgumentException {
    switch (databaseInfo.toLowerCase()) {
      case "mysql":
        return Server.MySQL;
      case "mariadb":
        return Server.MariaDB;
      default:
        throw new IllegalArgumentException("Unknown database: " + databaseInfo);
    }
  }

  public synchronized static MySQLConnectOptions startMariaDB() throws Exception {
    String tag;
    String specifiedVersion = System.getProperty("embedded.database.version");
    if (specifiedVersion == null || specifiedVersion.isEmpty()) {
      tag = "10.4";
    } else {
      // we would not check this
      tag = specifiedVersion;
    }

    mariadb = new GenericContainer("mariadb:" + tag)
      .withEnv("MYSQL_USER", "mysql")
      .withEnv("MYSQL_PASSWORD", "password")
      .withEnv("MYSQL_ROOT_PASSWORD", "password")
      .withEnv("MYSQL_DATABASE", "testschema")
      .withCommand("--max_allowed_packet=33554432 --max_prepared_stmt_count=16382 --character-set-server=utf8mb4 --collation-server=utf8mb4_general_ci")
      .withExposedPorts(3306)
      .withClasspathResourceMapping("init.sql", "/docker-entrypoint-initdb.d/create-mysql.sql", BindMode.READ_ONLY);
    mariadb.start();

    return new MySQLConnectOptions()
      .setPort(mariadb.getMappedPort(3306))
      .setHost(mariadb.getContainerIpAddress())
      .setDatabase("testschema")
      .setUser("mysql")
      .setPassword("password");
  }

  public synchronized static void stopMariaDB() throws Exception {
    if (mariadb != null) {
      try {
        mariadb.stop();
      } finally {
        mariadb = null;
      }
    }
  }
}

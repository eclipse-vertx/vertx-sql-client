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
import com.wix.mysql.Sources;
import com.wix.mysql.config.Charset;
import com.wix.mysql.config.MysqldConfig;
import com.wix.mysql.config.SchemaConfig;
import com.wix.mysql.distribution.Version;
import io.vertx.mysqlclient.MySQLConnectOptions;
import org.junit.rules.ExternalResource;

import java.util.HashMap;
import java.util.Map;

public class MySQLRule extends ExternalResource {

  private static EmbeddedMysql mysql;

  public synchronized static MySQLConnectOptions startMysql() throws Exception {
    MysqldConfig mysqldConfig = MysqldConfig.aMysqldConfig(getMySQLVersion())
      .withCharset(Charset.UTF8)
      .withUser("mysql", "password")
      .withPort(3306)
      .withServerVariable("max_allowed_packet", 32 * 1024 * 1024)
      .withServerVariable("max_prepared_stmt_count", 16382)
      .build();

    SchemaConfig schemaConfig = SchemaConfig.aSchemaConfig("testschema")
      .withCharset(Charset.UTF8)
      .withScripts(ScriptResolver.classPathScripts("init.sql"))
      .withScripts(Sources.fromString("CREATE USER 'superuser'@'localhost' IDENTIFIED BY 'password';GRANT ALL ON *.* TO 'superuser'@'localhost' WITH GRANT OPTION;"))
      .build();

    mysql = EmbeddedMysql.anEmbeddedMysql(mysqldConfig)
      .addSchema(schemaConfig)
      .addSchema("emptyschema")
      .start();

    return new MySQLConnectOptions()
      .setHost("localhost")
      .setPort(mysqldConfig.getPort())
      .setUser(mysqldConfig.getUsername())
      .setPassword(mysqldConfig.getPassword())
      .setDatabase(schemaConfig.getName());

  }

  public synchronized static void stopMysql() throws Exception {
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
    String specifiedVersion = System.getProperty("embedded.mysql.version");
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

  public boolean isUsingMySQL5_6() {
    return MySQLRule.getMySQLVersion() == Version.v5_6_latest;
  }

  private MySQLConnectOptions options;

  public MySQLConnectOptions options() {
    return new MySQLConnectOptions(options);
  }

  @Override
  protected void before() throws Throwable {
    options = startMysql();
  }

  @Override
  protected void after() {
    if (options != null) {
      try {
        stopMysql();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}

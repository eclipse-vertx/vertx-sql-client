package io.reactiverse.mysqlclient2;

import com.wix.mysql.EmbeddedMysql;
import com.wix.mysql.ScriptResolver;
import com.wix.mysql.config.Charset;
import com.wix.mysql.config.MysqldConfig;
import com.wix.mysql.config.SchemaConfig;
import com.wix.mysql.distribution.Version;
import io.reactiverse.pgclient.PgConnectOptions;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;

public abstract class MysqlTestBase {
  protected static PgConnectOptions options;
  private static EmbeddedMysql mysqld;

  @BeforeClass
  public static void before() throws Exception {
    options = startMysql();
  }

  @AfterClass
  public static void after() throws Exception {
    stopMysql();
  }

  public synchronized static PgConnectOptions startMysql() throws IOException {
    MysqldConfig mysqldConfig = MysqldConfig.aMysqldConfig(Version.v5_7_latest)
      .withCharset(Charset.UTF8)
      .withUser("mysql", "password")
      .withPort(3306)
      .build();

    SchemaConfig schemaConfig = SchemaConfig.aSchemaConfig("test-schema")
      .withCharset(Charset.UTF8)
      .withScripts(ScriptResolver.classPathScripts("init.sql"))
      .build();

    mysqld = EmbeddedMysql.anEmbeddedMysql(mysqldConfig)
      .addSchema(schemaConfig)
      .start();

    return new PgConnectOptions()
      .setHost("localhost")
      .setPort(mysqldConfig.getPort())
      .setUser(mysqldConfig.getUsername())
      .setPassword(mysqldConfig.getPassword())
      .setDatabase(schemaConfig.getName());
  }

  public synchronized static void stopMysql() {
    if (mysqld != null) {
      try {
        mysqld.stop();
      } finally {
        mysqld = null;
      }
    }
  }
}

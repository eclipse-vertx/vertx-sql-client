package com.julienviet.pgclient;


import junit.framework.TestCase;
import ru.yandex.qatools.embed.postgresql.PostgresExecutable;
import ru.yandex.qatools.embed.postgresql.PostgresProcess;
import ru.yandex.qatools.embed.postgresql.PostgresStarter;
import ru.yandex.qatools.embed.postgresql.config.AbstractPostgresConfig;
import ru.yandex.qatools.embed.postgresql.config.PostgresConfig;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static ru.yandex.qatools.embed.postgresql.distribution.Version.*;

public class JdbcTestBase extends TestCase {

  Connection con;

  protected void setUp() throws Exception {
    PostgresStarter<PostgresExecutable, PostgresProcess> runtime = PostgresStarter.getDefaultInstance();
    PostgresConfig config = new PostgresConfig(V9_5_0, new AbstractPostgresConfig.Net("localhost", 8083),
      new AbstractPostgresConfig.Storage("postgres"), new AbstractPostgresConfig.Timeout(),
      new AbstractPostgresConfig.Credentials("postgres", "postgres"));
    PostgresExecutable exec = runtime.prepare(config);
    PostgresProcess process  = exec.start();
    File f2 = new File("src/test/resources/create-postgres.sql");
    process.importFromFile(f2);
    con = DriverManager.getConnection("jdbc:postgresql://"
      + process.getConfig().net().host() + ":"
      + process.getConfig().net().port() + "/postgres", "postgres", "postgres");

  }

  protected void tearDown() throws SQLException {
    if (con != null) {
      con.close();
    }
  }
}

package com.julienviet.pgclient;

import de.flapdoodle.embed.process.config.IRuntimeConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;

import java.io.File;
import java.util.Collections;

import static ru.yandex.qatools.embed.postgresql.distribution.Version.Main.V9_6;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

public abstract class PgTestBase {

  private static EmbeddedPostgres postgres;
  static PgClientOptions options = new PgClientOptions();

  @BeforeClass
  public static void startPg() throws Exception {
    IRuntimeConfig config;
    File target = new File(System.getProperty("target.dir"));
    if (target.exists() && target.isDirectory()) {
      config = EmbeddedPostgres.cachedRuntimeConfig(target.toPath());
    } else {
      config = EmbeddedPostgres.defaultRuntimeConfig();
    }
    postgres = new EmbeddedPostgres(V9_6);
    postgres.start(config, "localhost", 8081, "postgres", "postgres", "postgres", Collections.emptyList());
    postgres.getProcess().get().importFromFile(new File("src/test/resources/create-postgres.sql"));
    options.setHost("localhost");
    options.setPort(8081);
    options.setUsername("postgres");
    options.setPassword("postgres");
    options.setDatabase("postgres");
  }

  @AfterClass
  public static void stopPg() throws Exception {
    postgres.stop();
  }
}

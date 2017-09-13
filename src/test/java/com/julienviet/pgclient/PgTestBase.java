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

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
package io.reactiverse.pgclient;

import io.reactiverse.pgclient.impl.PgConnectionUriParser;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.json.JsonObject;

import static java.lang.Integer.parseInt;
import static java.lang.System.getenv;

/**
 * @author Billy Yuan <billy112487983@gmail.com>
 */
@VertxGen
public interface PgConnectOptionsProvider {
  /**
   * Provide an {@link PgConnectOptions} configured with a connection URI.
   */
  static PgConnectOptions fromUri(String connectionUri) {
    JsonObject parsedConfiguration = PgConnectionUriParser.parse(connectionUri);
    if (parsedConfiguration == null) {
      return new PgConnectOptions();
    } else {
      return new PgConnectOptions(parsedConfiguration);
    }
  }

  /**
   * Provide an {@link PgConnectOptions} configured with environment variables.
   */
  static PgConnectOptions fromEnv() {
    PgConnectOptions pgConnectOptions = new PgConnectOptions();

    if (getenv("PGHOSTADDR") == null) {
      if (getenv("PGHOST") != null) {
        pgConnectOptions.setHost(getenv("PGHOST"));
      }
    } else {
      pgConnectOptions.setHost(getenv("PGHOSTADDR"));
    }

    if (getenv("PGPORT") != null) {
      try {
        pgConnectOptions.setPort(parseInt(getenv("PGPORT")));
      } catch (NumberFormatException e) {
        // port will be set to default
      }
    }

    if (getenv("PGDATABASE") != null) {
      pgConnectOptions.setDatabase(getenv("PGDATABASE"));
    }
    if (getenv("PGUSER") != null) {
      pgConnectOptions.setUsername(getenv("PGUSER"));
    }
    if (getenv("PGPASSWORD") != null) {
      pgConnectOptions.setPassword(getenv("PGPASSWORD"));
    }
    return pgConnectOptions;
  }
}

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

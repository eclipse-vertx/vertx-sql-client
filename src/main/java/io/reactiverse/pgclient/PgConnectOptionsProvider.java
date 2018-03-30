package io.reactiverse.pgclient;

import io.reactiverse.pgclient.impl.PgConnectionUriParser;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.json.JsonObject;

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
}

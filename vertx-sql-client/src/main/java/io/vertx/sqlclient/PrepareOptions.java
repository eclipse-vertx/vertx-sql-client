package io.vertx.sqlclient;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * Options for preparing a statement.
 *
 * Currently empty, custom options might be used by implementations to customize specific behavior.
 */
@DataObject
public class PrepareOptions {

  public PrepareOptions() {
  }

  public PrepareOptions(JsonObject json) {
  }
}

package com.julienviet.pgclient.impl;

import io.vertx.core.json.JsonArray;

import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface QueryResultHandler {

  void beginResult(List<String> columnNames);

  void handleRow(JsonArray row);

  void endResult(boolean suspended);

  void fail(Throwable cause);

  void end();

}

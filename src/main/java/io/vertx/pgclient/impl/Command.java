package io.vertx.pgclient.impl;

import io.vertx.pgclient.Result;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

public interface Command {
  void onSuccess(Result result);
  void onError(String message);
}

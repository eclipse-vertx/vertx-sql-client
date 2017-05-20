package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.Result;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

public interface Command {
  void onSuccess(Result result);
  void onError(String message);
}

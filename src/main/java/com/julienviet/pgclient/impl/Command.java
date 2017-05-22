package com.julienviet.pgclient.impl;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

public interface Command<T> {
  void onSuccess(T result);
  void onError(String message);
}

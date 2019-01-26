package io.reactiverse.mysqlclient.impl;

import io.reactiverse.mysqlclient.ImplReusable;

@ImplReusable
public class MySQLResultImpl<T> extends MySQLResultBase<T, MySQLResultImpl<T>> {
  private final T value;

  public MySQLResultImpl(T value) {
    this.value = value;
  }

  @Override
  public T value() {
    return value;
  }
}

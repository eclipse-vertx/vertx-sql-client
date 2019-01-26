package io.reactiverse.mysqlclient.impl;

import io.reactiverse.mysqlclient.ImplReusable;
import io.reactiverse.pgclient.PgResult;

import java.util.List;

@ImplReusable
public abstract class MySQLResultBase<T, R extends MySQLResultBase<T, R>> implements PgResult<T> {
  int updated;
  List<String> columnNames;
  int size;
  R next;

  @Override
  public List<String> columnsNames() {
    return columnNames;
  }

  @Override
  public int rowCount() {
    return updated;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public R next() {
    return next;
  }
}

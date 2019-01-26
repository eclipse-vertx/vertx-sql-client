package io.reactiverse.mysqlclient.impl;

import io.reactiverse.mysqlclient.ImplReusable;

@ImplReusable
public interface QueryResultHandler<T> {

  void handleResult(int updatedCount, int size, ColumnMetadata columnMetadata, T result);

}

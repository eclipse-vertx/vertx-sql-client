package io.vertx.sqlclient.impl;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlCredentials;
import io.vertx.sqlclient.SqlCredentialsProvider;

public class StaticSqlCredentialsProvider implements SqlCredentialsProvider {

  private final SqlCredentials credentials;

  public StaticSqlCredentialsProvider(String username, String password) {
    this.credentials = new SqlCredentials(username, password);
  }

  @Override
  public Future<SqlCredentials> getCredentials(Context context) {
    return Future.succeededFuture(credentials);
  }

}

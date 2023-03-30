package io.vertx.sqlclient;

import io.vertx.core.Context;
import io.vertx.core.Future;

public interface SqlCredentialsProvider {

  class Static implements SqlCredentialsProvider {

    private final Credentials credentials;

    public Static(String username, String password) {
      this.credentials = new Credentials(username, password);
    }

    @Override
    public Future<Credentials> getCredentials(Context context) {
      return Future.succeededFuture(credentials);
    }
  }

  class Credentials {

    public String username;
    public String password;

    public Credentials(String username, String password) {
      this.username = username;
      this.password = password;
    }

  }

  Future<Credentials> getCredentials(Context context);

}

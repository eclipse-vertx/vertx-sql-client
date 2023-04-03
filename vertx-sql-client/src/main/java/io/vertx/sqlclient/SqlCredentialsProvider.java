package io.vertx.sqlclient;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Context;
import io.vertx.core.Future;

@VertxGen
public interface SqlCredentialsProvider {

  Future<SqlCredentials> getCredentials(Context context);

}

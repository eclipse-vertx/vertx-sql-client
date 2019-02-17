package io.reactiverse.mysqlclient.impl;

import io.reactiverse.mysqlclient.ImplReusable;
import io.reactiverse.mysqlclient.impl.codec.MySQLCommandBase;
import io.reactiverse.mysqlclient.impl.codec.MySQLCommandResponse;
import io.vertx.core.Handler;

@ImplReusable
@FunctionalInterface
public interface CommandScheduler {

  <R> void schedule(MySQLCommandBase<R> cmd, Handler<? super MySQLCommandResponse<R>> handler);

}

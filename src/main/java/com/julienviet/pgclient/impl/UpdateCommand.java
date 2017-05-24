package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.decoder.message.ReadyForQuery;
import com.julienviet.pgclient.codec.encoder.message.Query;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.sql.UpdateResult;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

class UpdateCommand extends UpdateCommandBase {

  private final String sql;
  private final Handler<AsyncResult<UpdateResult>> handler;

  UpdateCommand(String sql, Handler<AsyncResult<UpdateResult>> handler) {
    this.handler = handler;
    this.sql = sql;
  }

  @Override
  boolean exec(DbConnection conn) {
    conn.writeToChannel(new Query(sql));
    return true;
  }

  @Override
  public boolean handleMessage(Message msg) {
    if (msg.getClass() == ReadyForQuery.class) {
      return true;
    } else {
      return super.handleMessage(msg);
    }
  }

  @Override
  void handleResult(UpdateResult result) {
    handler.handle(Future.succeededFuture(result));
  }


  @Override
  void fail(Throwable cause) {
    handler.handle(Future.failedFuture(cause));
  }

  public String getSql() {
    return sql;
  }
}

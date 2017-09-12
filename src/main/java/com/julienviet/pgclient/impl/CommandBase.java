package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.codec.Message;
import io.vertx.core.Handler;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

abstract class CommandBase {

  public void handleMessage(Message msg) {
    System.out.println(getClass().getSimpleName() + " should handle message " + msg);
  }

  abstract void exec(DbConnection conn, Handler<Void> handler);

  abstract void fail(Throwable err);

}

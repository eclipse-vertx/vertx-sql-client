package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.codec.Message;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

abstract class CommandBase {

  public boolean handleMessage(Message msg) {
    System.out.println(getClass().getSimpleName() + " should handle message " + msg);
    return false;
  }

  abstract boolean exec(DbConnection conn);

  abstract void fail(Throwable err);

}

package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.PgException;
import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.decoder.message.CommandComplete;
import com.julienviet.pgclient.codec.decoder.message.ErrorResponse;
import io.vertx.core.Handler;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

abstract class TxUpdateCommandBase extends CommandBase {

  protected Handler<Void> doneHandler;

  @Override
  public void handleMessage(Message msg) {
    if (msg.getClass() == CommandComplete.class) {
      handleResult(null);
    } else if (msg.getClass() == ErrorResponse.class) {
      ErrorResponse error = (ErrorResponse) msg;
      doneHandler.handle(null);
      fail(new RuntimeException(new PgException(error)));
    } else {
      super.handleMessage(msg);
    }
  }

  abstract void handleResult(Void result);

  abstract void fail(Throwable cause);

}

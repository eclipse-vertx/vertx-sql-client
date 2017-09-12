package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.PgException;
import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.decoder.message.CommandComplete;
import com.julienviet.pgclient.codec.decoder.message.ErrorResponse;
import io.vertx.core.Handler;
import io.vertx.ext.sql.UpdateResult;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

abstract class UpdateCommandBase extends CommandBase {

  private UpdateResult updateResult;
  protected Handler<Void> doneHandler;

  @Override
  public void handleMessage(Message msg) {
    if (msg.getClass() == CommandComplete.class) {
      CommandComplete complete = (CommandComplete) msg;
      updateResult = new UpdateResult();
      updateResult.setUpdated(complete.getRowsAffected());
      handleResult(updateResult);
    } else if (msg.getClass() == ErrorResponse.class) {
      ErrorResponse error = (ErrorResponse) msg;
      fail(new PgException(error));
    } else {
      super.handleMessage(msg);
    }
  }

  abstract void handleResult(UpdateResult updateResult);

  abstract void fail(Throwable cause);

}

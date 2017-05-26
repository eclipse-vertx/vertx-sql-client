package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.decoder.message.CommandComplete;
import com.julienviet.pgclient.codec.decoder.message.ErrorResponse;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

abstract class VoidCommandBase extends CommandBase {

  @Override
  public boolean handleMessage(Message msg) {
    if (msg.getClass() == CommandComplete.class) {
      handleResult(null);
      return false;
    } else if (msg.getClass() == ErrorResponse.class) {
      ErrorResponse error = (ErrorResponse) msg;
      fail(new RuntimeException(error.getMessage()));
      return false;
    } else {
      return super.handleMessage(msg);
    }
  }

  abstract void handleResult(Void result);

  abstract void fail(Throwable cause);

}

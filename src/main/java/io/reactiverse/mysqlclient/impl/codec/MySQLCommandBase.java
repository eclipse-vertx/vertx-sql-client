package io.reactiverse.mysqlclient.impl.codec;

import io.vertx.core.Handler;

public abstract class MySQLCommandBase<RESULT_TYPE> {
  private final byte commandType;

  private Handler<? super MySQLCommandResponse<RESULT_TYPE>> handler;
  private Throwable failure;
  private RESULT_TYPE result;

  public MySQLCommandBase(byte commandType) {
    this.commandType = commandType;
  }

  public abstract void exec(MySQLPacketEncoder out);

  public final Handler<? super MySQLCommandResponse<RESULT_TYPE>> getHandler() {
    return handler;
  }

  public final void setHandler(Handler<? super MySQLCommandResponse<RESULT_TYPE>> handler) {
    this.handler = handler;
  }

  public final Throwable getFailure() {
    return failure;
  }

  public final void setFailure(Throwable failure) {
    this.failure = failure;
  }

  public final RESULT_TYPE getResult() {
    return result;
  }

  public final void setResult(RESULT_TYPE result) {
    this.result = result;
  }

  public final byte getCommandType() {
    return this.commandType;
  }
}

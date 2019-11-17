package io.vertx.sqlclient.impl.command;

import io.vertx.core.Future;
import io.vertx.sqlclient.impl.TxStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.impl.NoStackTraceThrowable;

public class CommandResponse<R> {

  public static <R> CommandResponse<R> failure(String msg) {
    return failure(new NoStackTraceThrowable(msg), null);
  }

  public static <R> CommandResponse<R> failure(String msg, TxStatus txStatus) {
    return failure(new NoStackTraceThrowable(msg), txStatus);
  }

  public static <R> CommandResponse<R> failure(Throwable cause) {
    return failure(cause, null);
  }

  public static <R> CommandResponse<R> failure(Throwable cause, TxStatus txStatus) {
    return new CommandResponse<R>(Future.failedFuture(cause), txStatus);
  }

  public static <R> CommandResponse<R> success(R result) {
    return success(result, null);
  }

  public static <R> CommandResponse<R> success(R result, TxStatus txStatus) {
    return new CommandResponse<R>(Future.succeededFuture(result), txStatus);
  }

  // The connection that executed the command
  public CommandScheduler scheduler;
  public CommandBase<R> cmd;
  private final AsyncResult<R> res;
  private final TxStatus txStatus;

  public CommandResponse(AsyncResult<R> res, TxStatus txStatus) {
    this.res = res;
    this.txStatus = txStatus;
  }

  public TxStatus txStatus() {
    return txStatus;
  }

  public AsyncResult<R> toAsyncResult() {
    return res;
  }

}

package io.vertx.sqlclient.impl.command;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.impl.NoStackTraceThrowable;

public abstract class CommandResponse<R> implements AsyncResult<R> {

  public static <R> CommandResponse<R> failure(String msg) {
    return failure(new NoStackTraceThrowable(msg));
  }

  public static <R> CommandResponse<R> failure(Throwable cause) {
    return new CommandResponse<R>(Future.failedFuture(cause)) {
      @Override
      public R result() {
        return null;
      }
      @Override
      public Throwable cause() {
        return cause;
      }
      @Override
      public boolean succeeded() {
        return false;
      }
      @Override
      public boolean failed() {
        return true;
      }
    };
  }

  public static <R> CommandResponse<R> success(R result) {
    return new CommandResponse<R>(Future.succeededFuture(result)) {
      @Override
      public R result() {
        return result;
      }
      @Override
      public Throwable cause() {
        return null;
      }
      @Override
      public boolean succeeded() {
        return true;
      }
      @Override
      public boolean failed() {
        return false;
      }
    };
  }

  public CommandBase<R> cmd;
  private final AsyncResult<R> res;

  public CommandResponse(AsyncResult<R> res) {
  this.res = res;
  }

  public AsyncResult<R> toAsyncResult() {
    return res;
  }

}

package io.vertx.sqlclient.impl.command;

import io.vertx.core.AsyncResult;
import io.vertx.core.impl.NoStackTraceThrowable;

public abstract class CommandResponse<R> implements AsyncResult<R> {

  public static <R> CommandResponse<R> failure(String msg) {
    return failure(new NoStackTraceThrowable(msg));
  }

  public static <R> CommandResponse<R> failure(Throwable cause) {
    return new CommandResponse<R>() {
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
    return new CommandResponse<R>() {
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

  // The connection that executed the command
  public CommandScheduler scheduler;
  public CommandBase<R> cmd;

  public CommandResponse() {
  }

}

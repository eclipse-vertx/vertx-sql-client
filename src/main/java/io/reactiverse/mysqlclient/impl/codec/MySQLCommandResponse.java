package io.reactiverse.mysqlclient.impl.codec;

import io.reactiverse.mysqlclient.ImplReusable;
import io.vertx.core.AsyncResult;
import io.vertx.core.impl.NoStackTraceThrowable;

@ImplReusable
public abstract class MySQLCommandResponse<R> implements AsyncResult<R> {
  private MySQLCommandResponse() {
  }

  public static <R> MySQLCommandResponse<R> failure(String message) {
    return failure(new NoStackTraceThrowable(message));
  }

  public static <R> MySQLCommandResponse<R> failure(Throwable cause) {
    return new MySQLCommandResponse<R>() {
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

  public static <R> MySQLCommandResponse<R> success(R result) {
    return new MySQLCommandResponse<R>() {
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
}

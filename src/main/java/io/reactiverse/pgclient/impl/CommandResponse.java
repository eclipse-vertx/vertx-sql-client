package io.reactiverse.pgclient.impl;

import io.reactiverse.pgclient.impl.codec.TxStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.impl.NoStackTraceThrowable;

public abstract class CommandResponse<R> implements AsyncResult<R> {

  static <R> CommandResponse<R> failure(String msg) {
    return failure(new NoStackTraceThrowable(msg), null);
  }

  static <R> CommandResponse<R> failure(String msg, TxStatus txStatus) {
    return failure(new NoStackTraceThrowable(msg), txStatus);
  }

  static <R> CommandResponse<R> failure(Throwable cause) {
    return failure(cause, null);
  }

  static <R> CommandResponse<R> failure(Throwable cause, TxStatus txStatus) {
    return new CommandResponse<R>(txStatus) {
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

  static <R> CommandResponse<R> success(R result) {
    return success(result, null);
  }

  static <R> CommandResponse<R> success(R result, TxStatus txStatus) {
    return new CommandResponse<R>(txStatus) {
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

  private final TxStatus txStatus;

  public CommandResponse(TxStatus txStatus) {
    this.txStatus = txStatus;
  }

  TxStatus txStatus() {
    return txStatus;
  }

}

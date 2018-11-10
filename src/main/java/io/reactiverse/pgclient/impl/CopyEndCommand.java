package io.reactiverse.pgclient.impl;

import io.netty.buffer.ByteBuf;
import io.reactiverse.pgclient.PgException;
import io.reactiverse.pgclient.impl.codec.decoder.ErrorResponse;
import io.reactiverse.pgclient.impl.codec.encoder.MessageEncoder;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import java.util.function.Consumer;

class CopyEndCommand extends CommandBase<Integer> {

  private final Consumer<ByteBuf> endSignalWriter;

  CopyEndCommand(Consumer<ByteBuf> endSignalWriter, Handler<AsyncResult<Integer>> resultHandler) {
    this.endSignalWriter = endSignalWriter;
    this.handler = res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result()));
      }
    };
  }

  @Override
  void exec(MessageEncoder out) {
    out.writeCopyData(endSignalWriter);
    out.writeCopyEnd();
  }

  @Override
  public void handleErrorResponse(ErrorResponse errorResponse) {
    failure = new PgException(errorResponse);
  }

  @Override
  public void handleCommandComplete(int updated) {
    handler.handle(CommandResponse.success(updated));
  }
}

package io.reactiverse.pgclient.impl;

import io.netty.buffer.ByteBuf;
import io.reactiverse.pgclient.PgException;
import io.reactiverse.pgclient.impl.codec.decoder.ErrorResponse;
import io.reactiverse.pgclient.impl.codec.encoder.MessageEncoder;
import io.vertx.core.Handler;
import java.util.function.Consumer;

class CopyDataCommand extends CommandBase<Boolean> {

  private final Consumer<ByteBuf> writer;

  CopyDataCommand(Consumer<ByteBuf> writer, Handler<? super CommandResponse<Boolean>> handler) {
    this.writer = writer;
    this.handler = handler;
  }

  @Override
  void exec(MessageEncoder out) {
    out.writeCopyData(writer);
  }

  @Override
  public void handleErrorResponse(ErrorResponse errorResponse) {
    failure = new PgException(errorResponse);
  }

  public void dataAccepted() {
    handler.handle(CommandResponse.success(true));
  }
}

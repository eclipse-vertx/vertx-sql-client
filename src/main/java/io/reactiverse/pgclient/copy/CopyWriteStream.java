package io.reactiverse.pgclient.copy;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.streams.WriteStream;

@VertxGen
public interface CopyWriteStream<T> extends WriteStream<T> {

  /**
   * Sets the handler to be notified on the completion of a copy.
   * @param rowCount the number of rows added.
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  CopyWriteStream<T> endHandler(Handler<Integer> rowCount);
}

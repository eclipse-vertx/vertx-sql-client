package io.reactiverse.pgclient.copy;

import io.vertx.core.streams.ReadStream;

public interface PgCopyReadStream<T> extends ReadStream<T> {
  CopyFormat getFormat();
}

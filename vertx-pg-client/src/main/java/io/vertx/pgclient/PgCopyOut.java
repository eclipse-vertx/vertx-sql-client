/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.pgclient;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;

/**
 * COPY TO STDOUT result as a Vert.x ReadStream.
 *
 * The stream emits COPY data frames as Buffer chunks. Like other hot Vert.x
 * read streams, it is initially in flowing mode. Call {@link #pause()} before
 * installing a handler when explicit demand control is required.
 *
 * <p>Consuming the stream also advances the PostgreSQL connection. Therefore
 * {@link #completion()} may remain pending while the stream is paused or has
 * no data handler.</p>
 *
 * {@link #completion()} completes with the COPY row count when the server completes the COPY command.
 */
@VertxGen(concrete = false)
public interface PgCopyOut extends ReadStream<Buffer> {

  @Fluent
  @Override
  PgCopyOut exceptionHandler(Handler<Throwable> handler);

  @Fluent
  @Override
  PgCopyOut handler(Handler<Buffer> handler);

  @Fluent
  @Override
  PgCopyOut pause();

  @Fluent
  @Override
  PgCopyOut resume();

  @Fluent
  @Override
  PgCopyOut endHandler(Handler<Void> endHandler);

  @Fluent
  @Override
  PgCopyOut fetch(long amount);

  /**
   * Completion of the COPY command.
   * <p>
   * The future succeeds with the row count reported by PostgreSQL, once the server is ready for
   * the next command, so the connection can be used again as soon as it is notified. When
   * transport backpressure is active, the stream must be consumed for the client to observe the
   * server's completion.
   */
  Future<Integer> completion();
}

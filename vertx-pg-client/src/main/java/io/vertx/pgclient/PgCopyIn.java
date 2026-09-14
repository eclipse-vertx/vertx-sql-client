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
import io.vertx.core.streams.WriteStream;

/**
 * COPY FROM STDIN sink as a Vert.x WriteStream.
 *
 * The stream accepts Buffer chunks and sends them as CopyData messages.
 * {@link #completion()} completes with the COPY row count when the server completes the COPY command.
 */
@VertxGen(concrete = false)
public interface PgCopyIn extends WriteStream<Buffer> {

  @Fluent
  @Override
  PgCopyIn exceptionHandler(Handler<Throwable> handler);

  @Fluent
  @Override
  PgCopyIn setWriteQueueMaxSize(int maxSize);

  @Fluent
  @Override
  PgCopyIn drainHandler(Handler<Void> handler);

  /**
   * Completion of the COPY command.
   * <p>
   * The future succeeds with the row count reported by PostgreSQL, once the server is ready for
   * the next command, so the connection can be used again as soon as it is notified.
   */
  Future<Integer> completion();

  /**
   * Fail COPY with a CopyFail message (server will respond with ErrorResponse).
   */
  Future<Void> abort(String message);
}

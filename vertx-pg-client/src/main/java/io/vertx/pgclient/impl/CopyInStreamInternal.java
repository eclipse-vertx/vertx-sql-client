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

package io.vertx.pgclient.impl;

import io.netty.buffer.ByteBuf;
import io.vertx.core.Future;
import io.vertx.pgclient.PgCopyIn;

public interface CopyInStreamInternal extends PgCopyIn {

  interface Sink {

    boolean isWritable();

    void onWritable(Runnable cb);

    void setWatermarks(int maxBytes);

    void writeCopyData(ByteBuf buf);

    void writeCopyDone();

    void writeCopyFail(String message);

    void detach();
  }

  void attachSink(Sink sink);

  void detachSinkIfAny();

  void completeFromServer(int rowCount);

  void failFromServer(Throwable t);

  Future<PgCopyIn> readyFuture();
}

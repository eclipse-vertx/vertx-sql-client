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

import io.vertx.core.buffer.Buffer;

import java.util.Objects;

/**
 * An inbound COPY OUT event.
 *
 * <p>The PostgreSQL decoder emits these events through the regular Vert.x
 * connection pipeline so the socket's existing read queue remains the sole
 * owner of Netty auto-read state.</p>
 */
public final class CopyOutEvent {

  private enum Kind {
    /** A CopyData message. */
    DATA,
    /** CommandComplete, no more data will follow. */
    END,
    /** ReadyForQuery, the COPY command is done and the connection is free. */
    COMPLETED
  }

  private final CopyOutStreamImpl stream;
  private final Kind kind;
  private final Buffer data;
  private final int rowCount;

  private CopyOutEvent(CopyOutStreamImpl stream, Kind kind, Buffer data, int rowCount) {
    this.stream = Objects.requireNonNull(stream, "stream");
    this.kind = kind;
    this.data = data;
    this.rowCount = rowCount;
  }

  public static CopyOutEvent data(CopyOutStreamImpl stream, Buffer data) {
    return new CopyOutEvent(stream, Kind.DATA, Objects.requireNonNull(data, "data"), -1);
  }

  public static CopyOutEvent end(CopyOutStreamImpl stream, int rowCount) {
    return new CopyOutEvent(stream, Kind.END, null, rowCount);
  }

  public static CopyOutEvent completed(CopyOutStreamImpl stream) {
    return new CopyOutEvent(stream, Kind.COMPLETED, null, -1);
  }

  void dispatch() {
    switch (kind) {
      case DATA:
        stream.emit(data);
        break;
      case END:
        stream.end(rowCount);
        break;
      case COMPLETED:
        stream.commandCompleted();
        break;
    }
  }
}

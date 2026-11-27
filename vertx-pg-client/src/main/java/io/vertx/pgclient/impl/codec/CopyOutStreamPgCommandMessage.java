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

package io.vertx.pgclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.internal.buffer.BufferInternal;
import io.vertx.pgclient.impl.CopyOutEvent;
import io.vertx.pgclient.impl.CopyOutStreamCommand;
import io.vertx.pgclient.impl.CopyOutStreamImpl;

final class CopyOutStreamPgCommandMessage
  extends PgCommandMessage<Void, CopyOutStreamCommand>
  implements CopyOutHandler {

  private final int aggregationThreshold;
  private final CopyOutStreamImpl out;
  private PgEncoder encoder;
  private Buffer agg;

  CopyOutStreamPgCommandMessage(CopyOutStreamCommand cmd) {
    super(cmd);
    this.out = cmd.out();
    this.aggregationThreshold = cmd.options().getAggregationThreshold();
    this.agg = aggregationThreshold == 1 ? null : Buffer.buffer(aggregationThreshold);
  }

  @Override
  void encode(PgEncoder encoder) {
    this.encoder = encoder;
    encoder.writeQuery(new QueryMessage(cmd.sql()));
  }

  @Override
  public void handleCopyOutResponse(int overall, short[] colFmts) {
    out.readyFromServer();
  }

  @Override
  public void handleCopyData(ByteBuf data) {
    if (failure != null || out.isDiscarding()) {
      data.release();
      return;
    }

    if (aggregationThreshold == 1) {
      fireData(BufferInternal.safeBuffer(data));
      return;
    }

    try {
      int len = data.readableBytes();

      if (agg.length() > 0 && agg.length() + len >= aggregationThreshold) {
        flushAgg();
      }

      ((BufferInternal) agg).unwrap().writeBytes(data, len);

      if (agg.length() >= aggregationThreshold) {
        flushAgg();
      }
    } finally {
      data.release();
    }
  }

  @Override
  public void handleCopyDone() {
    // Nothing to do, CommandComplete carries the row count and ends the stream
  }

  @Override
  public void handleCommandComplete(int updated) {
    if (out.isDiscarding()) {
      result = null;
      return;
    } else if (failure != null) {
      out.fail(failure);
    } else {
      flushAgg();
      fireEnd(updated);
    }

    result = null;
  }

  @Override
  void handleReadyForQuery() {
    super.handleReadyForQuery();
    // Same path as the data and end events, so it cannot overtake them
    fireCompleted();
  }

  @Override
  void handleErrorResponse(ErrorResponse errorResponse) {
    // Recorded, not reported: the command completion fails the stream, after ReadyForQuery
    failure = errorResponse.toException();
  }

  private void flushAgg() {
    if (agg != null && agg.length() > 0) {
      fireData(agg);
      agg = Buffer.buffer(aggregationThreshold);
    }
  }

  private void fireData(Buffer data) {
    encoder.channelHandlerContext().fireChannelRead(CopyOutEvent.data(out, data));
  }

  private void fireEnd(int rowCount) {
    encoder.channelHandlerContext().fireChannelRead(CopyOutEvent.end(out, rowCount));
  }

  private void fireCompleted() {
    encoder.channelHandlerContext().fireChannelRead(CopyOutEvent.completed(out));
  }
}

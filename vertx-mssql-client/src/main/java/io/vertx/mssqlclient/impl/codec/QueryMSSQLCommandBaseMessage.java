/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.internal.RowDescriptorBase;
import io.vertx.sqlclient.spi.protocol.QueryCommandBase;

import java.util.stream.Collector;

abstract class QueryMSSQLCommandBaseMessage<T, C extends QueryCommandBase<T>> extends MSSQLCommandMessage<Boolean, C> {

  protected int rowCount;
  protected RowResultDecoder<?, T> rowResultDecoder;

  QueryMSSQLCommandBaseMessage(C cmd) {
    super(cmd);
  }

  private static <A, T> T emptyResult(Collector<Row, A, T> collector) {
    return collector.finisher().apply(collector.supplier().get());
  }

  @Override
  protected void handleRowDesc(MSSQLRowDescriptor mssqlRowDesc) {
    rowResultDecoder = new RowResultDecoder<>(cmd.collector(), mssqlRowDesc);
  }

  @Override
  protected void handleRow(ByteBuf payload) {
    rowResultDecoder.nbc = false;
    rowResultDecoder.handleRow(-1, payload);
  }

  @Override
  protected void handleNbcRow(ByteBuf payload) {
    rowResultDecoder.nbc = true;
    rowResultDecoder.handleRow(-1, payload);
  }

  @Override
  protected void handleAffectedRows(long count) {
    rowCount = (int) Math.min(count, Integer.MAX_VALUE);
  }

  protected void handleResultSetDone() {
    this.result = false;
    T result;
    Throwable failure;
    int size;
    RowDescriptorBase rowDescriptor;
    if (rowResultDecoder != null) {
      failure = rowResultDecoder.complete();
      result = rowResultDecoder.result();
      rowDescriptor = rowResultDecoder.desc();
      size = rowResultDecoder.size();
      rowResultDecoder.reset();
    } else {
      result = emptyResult(cmd.collector());
      failure = null;
      size = 0;
      rowDescriptor = null;
    }
    cmd.resultHandler().handleResult(rowCount, size, rowDescriptor, result, failure);
  }
}


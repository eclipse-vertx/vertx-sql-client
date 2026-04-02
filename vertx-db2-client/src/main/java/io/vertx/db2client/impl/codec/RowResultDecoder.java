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
package io.vertx.db2client.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.db2client.impl.DB2RowImpl;
import io.vertx.db2client.impl.drda.Cursor;
import io.vertx.db2client.impl.drda.DRDAQueryResponse;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.data.Numeric;
import io.vertx.sqlclient.impl.RowDecoder;
import io.vertx.sqlclient.impl.RowInternal;

import java.math.BigDecimal;
import java.util.stream.Collector;

class RowResultDecoder<C, R> extends RowDecoder<C, R> {

  private static final Logger LOG = LoggerFactory.getLogger(RowResultDecoder.class);

  final DB2RowDesc rowDesc;
  final Cursor cursor;
  final DRDAQueryResponse response;

  RowResultDecoder(Collector<Row, C, R> collector, DB2RowDesc rowDesc, Cursor cursor, DRDAQueryResponse resp) {
    super(collector);
    this.rowDesc = rowDesc;
    this.cursor = cursor;
    this.response = resp;
  }

  public boolean isQueryComplete() {
    return response.isQueryComplete();
  }

  public boolean next() {
    response.readOpenQueryData();
    return cursor.next();
  }

  @Override
  protected RowInternal row() {
    return new DB2RowImpl(rowDesc);
  }

  @Override
  protected boolean decodeRow(int len, ByteBuf in, Row row) {
    for (int i = 1; i < rowDesc.columnDefinitions().columns_ + 1; i++) {
      int startingIdx = cursor.dataBuffer_.readerIndex();
      Object o = cursor.getObject(i);
      int endingIdx = cursor.dataBuffer_.readerIndex();
      // TODO: Remove this once all getObject paths are implemented safely
      // or add unit tests for this in the DRDA project
      if (startingIdx != endingIdx) {
        if (LOG.isWarnEnabled()) {
          LOG.warn("Reader index changed while getting data. Changed from " + startingIdx + " to "
            + endingIdx + " while obtaining object " + o);
        }
      }
      if (o instanceof BigDecimal) {
        o = Numeric.create((BigDecimal) o);
      }
      row.addValue(o);
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("decoded row values: " + row.deepToString());
    }
    return true;
  }
}

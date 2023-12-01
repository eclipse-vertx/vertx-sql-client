/*
 * Copyright (C) 2019,2020 IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertx.db2client.impl.codec;

import java.math.BigDecimal;
import java.util.stream.Collector;

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
        System.out.println("WARN: Reader index changed while getting data. Changed from " + startingIdx + " to "
            + endingIdx + " while obtaining object " + o);
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

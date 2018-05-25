/*
 * Copyright (C) 2017 Julien Viet
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
 *
 */

package io.reactiverse.pgclient.impl;

import io.reactiverse.pgclient.PgResult;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.impl.codec.ColumnDesc;
import io.reactiverse.pgclient.impl.codec.DataFormat;
import io.reactiverse.pgclient.impl.codec.DataTypeCodec;
import io.reactiverse.pgclient.impl.codec.decoder.ResultDecoder;
import io.reactiverse.pgclient.impl.codec.decoder.RowDescription;
import io.netty.buffer.ByteBuf;

import java.util.function.BiConsumer;
import java.util.stream.Collector;

public class RowResultDecoder<C, R> implements ResultDecoder<R> {

  private final Collector<Row, C, R> collector;
  private final BiConsumer<C, Row> accumulator;

  private RowDescription desc;
  private int size;
  private C container;

  RowResultDecoder(Collector<Row, C, R> collector, RowDescription desc) {
    this.collector = collector;
    this.accumulator = collector.accumulator();
    this.desc = desc;
  }

  @Override
  public void decodeRow(int len, ByteBuf in) {
    if (container == null) {
      container = collector.supplier().get();
    }
    Row row = new RowImpl(desc);
    for (int c = 0; c < len; ++c) {
      int length = in.readInt();
      Object decoded = null;
      if (length != -1) {
        ColumnDesc columnDesc = desc.columns()[c];
        if (columnDesc.getDataFormat() == DataFormat.BINARY) {
          decoded = DataTypeCodec.decodeBinary(columnDesc.getDataType(), length, in);
        } else {
          decoded = DataTypeCodec.decodeText(columnDesc.getDataType(), length, in);
        }
      }
      row.addValue(decoded);
    }
    accumulator.accept(container, row);
    size++;
  }

  @Override
  public PgResult<R> complete(int updated) {
    if (container == null) {
      container = collector.supplier().get();
    }
    R r = collector.finisher().apply(container);
    PgResultImpl<R> result = new PgResultImpl<>(updated, desc != null ? desc.columnNames() : null, r, size);
    container = null;
    // head = null;
    size = 0;
    return result;
  }
}

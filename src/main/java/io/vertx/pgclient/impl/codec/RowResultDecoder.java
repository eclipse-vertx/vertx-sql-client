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

package io.vertx.pgclient.impl.codec;

import io.vertx.sqlclient.Row;
import io.vertx.pgclient.impl.RowImpl;
import io.netty.buffer.ByteBuf;
import io.vertx.sqlclient.impl.RowDecoder;

import java.util.function.BiConsumer;
import java.util.stream.Collector;

class RowResultDecoder<C, R> implements RowDecoder {

  final Collector<Row, C, R> collector;
  final boolean singleton;
  final BiConsumer<C, Row> accumulator;
  final PgRowDesc desc;

  private int size;
  private C container;
  private Row row;

  RowResultDecoder(Collector<Row, C, R> collector, boolean singleton, PgRowDesc desc) {
    this.collector = collector;
    this.singleton = singleton;
    this.accumulator = collector.accumulator();
    this.desc = desc;
  }

  public int size() {
    return size;
  }

  @Override
  public void decodeRow(int len, ByteBuf in) {
    if (container == null) {
      container = collector.supplier().get();
    }
    if (singleton) {
      if (row == null) {
        row = new RowImpl(desc);
      } else {
        row.clear();
      }
    } else {
      row = new RowImpl(desc);
    }
    Row row = new RowImpl(desc);
    for (int c = 0; c < len; ++c) {
      int length = in.readInt();
      Object decoded = null;
      if (length != -1) {
        PgColumnDesc columnDesc = desc.columns[c];
        if (columnDesc.dataFormat == DataFormat.BINARY) {
          decoded = DataTypeCodec.decodeBinary(columnDesc.dataType, in.readerIndex(), length, in);
        } else {
          decoded = DataTypeCodec.decodeText(columnDesc.dataType, in.readerIndex(), length, in);
        }
        in.skipBytes(length);
      }
      row.addValue(decoded);
    }
    accumulator.accept(container, row);
    size++;
  }

  R complete() {
    if (container == null) {
      container = collector.supplier().get();
    }
    return collector.finisher().apply(container);
  }

  void reset() {
    container = null;
    size = 0;
  }
}

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
import io.vertx.pgclient.impl.PgRow;
import io.netty.buffer.ByteBuf;
import io.vertx.sqlclient.impl.RowDecoder;
import io.vertx.sqlclient.internal.RowInternal;

import java.util.stream.Collector;

public class RowResultDecoder<C, R> extends RowDecoder<C, R> {

  final PgRowDescriptor desc;

  public RowResultDecoder(Collector<Row, C, R> collector, PgRowDescriptor desc) {
    super(collector);
    this.desc = desc;
  }

  @Override
  protected RowInternal row() {
    return new PgRow(desc);
  }

  @Override
  protected boolean decodeRow(int len, ByteBuf in, Row row) {
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
    return true;
  }
}

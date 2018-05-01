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
import io.reactiverse.pgclient.impl.codec.decoder.message.RowDescription;
import io.netty.buffer.ByteBuf;

public class RowResultDecoder implements ResultDecoder<Row> {

  private RowDescription desc;
  private RowImpl head;
  private RowImpl tail;
  private int size;

  public RowResultDecoder() {
  }

  @Override
  public void init(RowDescription desc) {
    this.desc = desc;
  }

  @Override
  public void decodeRow(int len, ByteBuf in) {
    RowImpl row = new RowImpl(desc);
    for (int c = 0; c < len; ++c) {
      int length = in.readInt();
      if (length != -1) {
        ColumnDesc columnDesc = desc.columns()[c];
        Object decoded;
        if (columnDesc.getDataFormat() == DataFormat.BINARY) {
          decoded = DataTypeCodec.decodeBinary(columnDesc.getDataType(), length, in);
        } else {
          decoded = DataTypeCodec.decodeText(columnDesc.getDataType(), length, in);
        }
        if(decoded != null) {
          row.add(decoded);
        } else {
          row.add(null);
        }
      } else {
        row.add(null);
      }
    }
    if (head == null) {
      head = tail = row;
    } else {
      tail.next = row;
      tail = row;
    }
    size++;
  }

  @Override
  public PgResult<Row> complete(int updated) {
    PgResultImpl result = new PgResultImpl(updated, desc != null ? desc.columnNames() : null, head, size);
    head = null;
    size = 0;
    return result;
  }
}

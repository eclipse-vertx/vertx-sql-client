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

package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.PgResult;
import com.julienviet.pgclient.Tuple;
import com.julienviet.pgclient.impl.codec.Column;
import com.julienviet.pgclient.impl.codec.DataType;
import com.julienviet.pgclient.impl.codec.decoder.ResultDecoder;
import com.julienviet.pgclient.impl.codec.decoder.message.RowDescription;
import io.netty.buffer.ByteBuf;

public class RowResultDecoder implements ResultDecoder<Tuple> {

  private RowDescription desc;
  private LinkedArrayTuple head;
  private LinkedArrayTuple tail;
  private int size;

  public RowResultDecoder() {
  }

  @Override
  public void init(RowDescription desc) {
    this.desc = desc;
  }

  @Override
  public void decodeRow(int len, ByteBuf in) {
    LinkedArrayTuple row = new LinkedArrayTuple(desc.columns().length);
    for (int c = 0; c < len; ++c) {
      int length = in.readInt();
      if (length != -1) {
        Column columnDesc = desc.columns()[c];
        DataType.Decoder decoder = columnDesc.getCodec();
        Object decoded = decoder.decode(length, in);
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
  public PgResult<Tuple> complete(int updated) {
    PgResultImpl result = new PgResultImpl(updated, desc != null ? desc.columnNames() : null, head, size);
    head = null;
    size = 0;
    return result;
  }
}

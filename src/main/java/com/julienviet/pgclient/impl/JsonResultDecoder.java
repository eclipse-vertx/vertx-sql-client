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
import com.julienviet.pgclient.PgRow;
import com.julienviet.pgclient.codec.Column;
import com.julienviet.pgclient.codec.DataType;
import com.julienviet.pgclient.codec.decoder.ResultDecoder;
import com.julienviet.pgclient.codec.decoder.message.RowDescription;
import io.netty.buffer.ByteBuf;

public class JsonResultDecoder implements ResultDecoder<PgRow> {

  private RowDescription desc;
  private PgRowImpl head;
  private PgRowImpl tail;
  private int size;

  public JsonResultDecoder() {
  }

  @Override
  public void init(RowDescription desc) {
    this.desc = desc;
  }

  @Override
  public void decodeRow(int len, ByteBuf in) {
    PgRowImpl row = new PgRowImpl(desc.getColumns().length);
    for (int c = 0; c < len; ++c) {
      int length = in.readInt();
      if (length != -1) {
        Column columnDesc = desc.getColumns()[c];
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
  public PgResult<PgRow> complete() {
    PgResultImpl result = new PgResultImpl(desc != null ? desc.getColumnNames() : null, head, size);
    head = null;
    size = 0;
    return result;
  }
}

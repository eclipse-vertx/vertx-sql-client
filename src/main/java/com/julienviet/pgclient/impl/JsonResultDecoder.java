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

import com.julienviet.pgclient.PgRow;
import com.julienviet.pgclient.codec.DataType;
import com.julienviet.pgclient.codec.decoder.ResultDecoder;
import com.julienviet.pgclient.codec.decoder.message.RowDescription;
import io.netty.buffer.ByteBuf;

public class JsonResultDecoder implements ResultDecoder<PgRow> {

  private RowDescription desc;
  private QueryResultHandler<PgRow> handler;

  public JsonResultDecoder(QueryResultHandler<PgRow> handler) {
    this.handler = handler;
  }

  @Override
  public void init(RowDescription desc) {
    handler.beginRows(desc.getColumnNames());
    this.desc = desc;
  }

  public PgRow createRow(int size) {
    return new JsonPgRow(size);
  }

  @Override
  public void decodeColumnToRow(PgRow row, ByteBuf in, int len, DataType.Decoder decoder) {
    JsonPgRow a = (JsonPgRow) row;
    if (len != -1) {
      Object decoded = decoder.decode(len, in);
      if(decoded != null) {
        a.add(decoded);
      } else {
        a.add(null);
      }
    } else {
      a.add(null);
    }
  }

  @Override
  public void addRow(PgRow row) {
    handler.addRow(row);
  }

  public void complete() {
    handler.endRows();
  }
}

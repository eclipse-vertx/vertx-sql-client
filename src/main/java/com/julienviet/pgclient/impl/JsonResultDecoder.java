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
import com.julienviet.pgclient.codec.DataFormat;
import com.julienviet.pgclient.codec.DataType;
import com.julienviet.pgclient.codec.decoder.ResultDecoder;
import io.netty.buffer.ByteBuf;
import io.vertx.core.json.JsonArray;

public class JsonResultDecoder implements ResultDecoder<PgRow> {

  private QueryResultHandler handler;

  public JsonResultDecoder(QueryResultHandler handler) {
    this.handler = handler;
  }

  public PgRow createRow(int len) {
    return new JsonPgRow(len);
  }

  @Override
  public void decode(ByteBuf in, int len, DataType<?> dataType, DataFormat format, PgRow row) {
    JsonPgRow a = (JsonPgRow) row;
    if (len != -1) {
      Object decoded;
      if (format == DataFormat.TEXT) {
        decoded = dataType.decodeText(len, in);
      } else {
        decoded = dataType.decodeBinary(len, in);
      }
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

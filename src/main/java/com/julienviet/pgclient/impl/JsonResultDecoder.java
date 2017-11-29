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

import com.julienviet.pgclient.codec.Column;
import com.julienviet.pgclient.codec.DataFormat;
import com.julienviet.pgclient.codec.DataType;
import com.julienviet.pgclient.codec.decoder.ResultDecoder;
import com.julienviet.pgclient.codec.decoder.message.RowDescription;
import io.netty.buffer.ByteBuf;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;

import java.util.ArrayList;
import java.util.List;

public class JsonResultDecoder implements ResultDecoder {

  private List<JsonArray> rows;
  private Handler<List<JsonArray>> handler;

  public JsonResultDecoder(Handler<List<JsonArray>> handler) {
    this.handler = handler;
  }

  public void decode(ByteBuf in, RowDescription rowDesc, DataFormat format) {
    int len = in.readUnsignedShort();
    JsonArray row = new JsonArray(new ArrayList(len));
    for (int c = 0; c < len; ++c) {
      int length = in.readInt();
      if (length != -1) {
        Column columnDesc = rowDesc.getColumns()[c];
        DataType dataType = columnDesc.getDataType();
        Object decoded;
        if (format == DataFormat.TEXT) {
          decoded = dataType.decodeText(length, in);
        } else {
          decoded = dataType.decodeBinary(length, in);
        }
        if(decoded != null) {
          row.add(decoded);
        } else {
          row.addNull();
        }
      } else {
        row.addNull();
      }
    }
    if (rows == null) {
      rows = new ArrayList<>();
    }
    rows.add(row);
  }

  public void complete() {
    if (rows != null) {
      handler.handle(rows);
      rows = null;
    }
  }
}

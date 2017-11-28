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

package com.julienviet.pgclient.codec.decoder.message;

import com.julienviet.pgclient.codec.Column;
import com.julienviet.pgclient.codec.DataFormat;
import com.julienviet.pgclient.codec.decoder.InboundMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.vertx.core.json.JsonArray;

import java.util.Arrays;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class DataRow implements InboundMessage {

  final byte[][] values;

  public DataRow(byte[][] values) {
    this.values = values;
  }
  public Object getValue(int i) {
    return values[i];
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DataRow that = (DataRow) o;
    return Arrays.equals(values, that.values);
  }

  public JsonArray decode(RowDescription desc, DataFormat format) {
    JsonArray array = new JsonArray();
    for (int i = 0;i < values.length;i++) {
      byte[] value = values[i];
      if (value == null) {
        array.addNull();
      } else {
        Column column = desc.getColumns()[i];
        ByteBuf bb = Unpooled.copiedBuffer(value);
        Object decoded;
        if (format == DataFormat.TEXT) {
          decoded = column.getDataType().decodeText(value.length, bb);
        } else {
          decoded = column.getDataType().decodeBinary(value.length, bb);
        }
        array.add(decoded);
      }
    }
    return array;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(values);
  }

  @Override
  public String toString() {
    return "DataRow{" +
      "values=" + Arrays.toString(values) +
      '}';
  }
}

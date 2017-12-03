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

package com.julienviet.pgclient.codec.decoder;

import com.julienviet.pgclient.PgResult;
import com.julienviet.pgclient.PgRowIterator;
import com.julienviet.pgclient.codec.DataType;
import com.julienviet.pgclient.codec.decoder.message.RowDescription;
import io.netty.buffer.ByteBuf;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

public interface ResultDecoder<T> {

  void init(RowDescription desc);
  T createRow(int size);
  void decodeColumnToRow(T row, ByteBuf in, int len, DataType.Decoder decoder);
  void addRow(T row);
  PgResult<T> complete();
}

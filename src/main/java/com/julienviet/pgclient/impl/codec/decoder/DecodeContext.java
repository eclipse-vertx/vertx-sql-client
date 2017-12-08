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

package com.julienviet.pgclient.impl.codec.decoder;

import com.julienviet.pgclient.impl.codec.DataFormat;
import com.julienviet.pgclient.impl.codec.decoder.message.RowDescription;

public class DecodeContext {

  final boolean peekDesc;
  final RowDescription rowDesc;
  final DataFormat dataFormat;
  final ResultDecoder decoder;
  RowDescription current;

  public DecodeContext(boolean peekDesc, RowDescription rowDesc, DataFormat dataFormat, ResultDecoder decoder) {
    this.peekDesc = peekDesc;
    this.rowDesc = rowDesc;
    this.dataFormat = dataFormat;
    this.decoder = decoder;
  }
}

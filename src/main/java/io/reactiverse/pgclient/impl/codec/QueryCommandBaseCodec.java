/*
 * Copyright (C) 2018 Julien Viet
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
package io.reactiverse.pgclient.impl.codec;

import io.reactiverse.sqlclient.Row;
import io.reactiverse.sqlclient.impl.RowDesc;
import io.reactiverse.sqlclient.impl.command.QueryCommandBase;

import java.util.stream.Collector;

abstract class QueryCommandBaseCodec<T, C extends QueryCommandBase<T>> extends PgCommandCodec<Boolean, C> {

  RowResultDecoder<?, T> decoder;

  QueryCommandBaseCodec(C cmd) {
    super(cmd);
  }

  @Override
  public void handleCommandComplete(int updated) {
    this.result = false;
    T result;
    int size;
    RowDesc desc;
    if (decoder != null) {
      result = decoder.complete();
      desc = decoder.desc;
      size = decoder.size();
      decoder.reset();
    } else {
      result = emptyResult(cmd.collector());
      size = 0;
      desc = null;
    }
    cmd.resultHandler().handleResult(updated, size, desc, result);
  }

  @Override
  public void handleErrorResponse(ErrorResponse errorResponse) {
    failure = errorResponse.toException();
  }

  private static <A, T> T emptyResult(Collector<Row, A, T> collector) {
    return collector.finisher().apply(collector.supplier().get());
  }
}

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

package io.vertx.sqlclient.impl;

import io.netty.buffer.ByteBuf;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.internal.RowInternal;

import java.util.function.BiConsumer;
import java.util.stream.Collector;

public abstract class RowDecoder<C, R> {

  private final Collector<Row, C, R> collector;
  private BiConsumer<C, Row> accumulator;

  private RowInternal row;
  private int size;
  private C container;
  private Throwable failure;
  private R result;

  protected RowDecoder(Collector<Row, C, R> collector) {
    this.collector = collector;

    reset();
  }

  protected abstract RowInternal row();

  public int size() {
    return size;
  }

  protected abstract boolean decodeRow(int len, ByteBuf in, Row row);

  public void handleRow(int len, ByteBuf in) {
    RowInternal r = row;
    if (r == null) {
      r = row();
    } else {
      row = null;
    }
    boolean decoded = decodeRow(len, in, r);
    if (decoded && failure == null) {
      if (accumulator == null) {
        try {
          accumulator = collector.accumulator();
        } catch (Exception e) {
          failure = e;
          return;
        }
      }
      try {
        accumulator.accept(container, r);
      } catch (Exception e) {
        failure = e;
        return;
      }
      if (r.tryRecycle()) {
        row = r;
      }
      size++;
    }
  }

  public R result() {
    return result;
  }

  public Throwable complete() {
    try {
      result = collector.finisher().apply(container);
    } catch (Exception e) {
      failure = e;
    }
    return failure;
  }

  public void reset() {
    size = 0;
    failure = null;
    result = null;
    try {
      this.container = collector.supplier().get();
    } catch (Exception e) {
      failure = e;
    }
  }
}

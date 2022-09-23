/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.sqlclient.benchmarks;

import io.vertx.sqlclient.impl.accumulator.ArrayListRowAccumulator;
import io.vertx.sqlclient.impl.accumulator.ChunkedRowAccumulator;
import io.vertx.sqlclient.impl.accumulator.RowAccumulator;

import java.util.function.IntUnaryOperator;

public enum RowAccumulatorType {

  ARRAY_LIST, CHUNKED_FIXED_SIZE, CHUNKED_GROWING_SIZE;

  RowAccumulator<String> newInstance() {
    switch (this) {
      case ARRAY_LIST:
        return new ArrayListRowAccumulator<>();
      case CHUNKED_FIXED_SIZE:
        return new ChunkedRowAccumulator<>(IntUnaryOperator.identity());
      case CHUNKED_GROWING_SIZE:
        return new ChunkedRowAccumulator<>(size -> size + (size >> 1));
      default:
        throw new AssertionError();
    }
  }
}

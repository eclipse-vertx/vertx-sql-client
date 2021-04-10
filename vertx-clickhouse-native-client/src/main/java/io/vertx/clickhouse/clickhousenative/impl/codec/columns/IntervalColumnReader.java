/*
 *
 *  Copyright (c) 2021 Vladimir Vishnevskii
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Eclipse Public License 2.0 which is available at
 *  http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 *  which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;

import java.time.Duration;

public class IntervalColumnReader extends UInt64ColumnReader {
  private final Duration multiplier;

  public IntervalColumnReader(int nRows, ClickhouseNativeColumnDescriptor descriptor, Duration multiplier) {
    super(nRows, descriptor);
    this.multiplier = multiplier;
  }

  @Override
  protected Object getElementInternal(int rowIdx, Class<?> desired) {
    Long obj = (Long)super.getElementInternal(rowIdx, desired);
    if (desired != Duration.class) {
      return obj;
    }
    return multiplier.multipliedBy(obj);
  }
}

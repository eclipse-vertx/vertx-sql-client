/*
 *
 *  * Copyright (c) 2021 Vladimir Vishnevsky
 *  *
 *  * This program and the accompanying materials are made available under the
 *  * terms of the Eclipse Public License 2.0 which is available at
 *  * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 *  * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  *
 *  * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;

import java.util.Map;

public class Enum8ColumnReader extends UInt8ColumnReader implements EnumColumnReader {
  public static final int ELEMENT_SIZE = 1;
  private final EnumColumnDecoder columnRecoder;

  public Enum8ColumnReader(int nRows, ClickhouseNativeColumnDescriptor descr, Map<? extends Number, String> enumVals, EnumResolutionMethod resolutionMethod) {
    super(nRows, descr);
    this.columnRecoder = new EnumColumnDecoder(enumVals, resolutionMethod);
  }

  @Override
  protected Object getElementInternal(int rowIdx, Class desired) {
    Byte key = (Byte) super.getElementInternal(rowIdx, desired);
    return columnRecoder.recodeElement(key, desired);
  }

  @Override
  public Object[] recodeValues(Object[] src, Class desired) {
    return columnRecoder.recodeValues(src, desired);
  }
}

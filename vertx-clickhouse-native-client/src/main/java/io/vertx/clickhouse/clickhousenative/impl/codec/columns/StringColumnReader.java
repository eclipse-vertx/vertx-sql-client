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

import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class StringColumnReader extends ClickhouseColumnReader {
  private Integer curStrLength;
  private List<byte[]> elements;
  private final Charset charset;
  private final boolean enableStringCache;
  private final StringCache cache;

  protected StringColumnReader(int nRows, ClickhouseNativeColumnDescriptor descriptor, boolean enableStringCache, ClickhouseNativeDatabaseMetadata md) {
    super(nRows, descriptor);
    this.elements = new ArrayList<>(nRows);
    this.charset = md.getStringCharset();
    this.enableStringCache = enableStringCache;
    this.cache = enableStringCache ? new StringCache(nRows) : null;
  }

  @Override
  protected Object[] readItems(ClickhouseStreamDataSource in) {
    while (elements.size() < nRows) {
      if (curStrLength == null) {
        curStrLength = in.readULeb128();
        if (curStrLength == null) {
          return null;
        }
      }
      if (in.readableBytes() < curStrLength) {
        return null;
      }
      //no dedicated BLOB type support; will encode(later) into String if user asked for String explicitly
      byte[] stringBytes;
      if (nullsMap == null || !nullsMap.get(elements.size())) {
        stringBytes = new byte[curStrLength];
        in.readBytes(stringBytes);
      } else {
        stringBytes = null;
        in.skipBytes(curStrLength);
      }
      elements.add(stringBytes);
      curStrLength = null;
    }
    Object[] ret = elements.toArray(new byte[elements.size()][]);
    elements = null;
    return ret;
  }

  @Override
  protected Object getElementInternal(int rowIdx, Class<?> desired) {
    Object tmp = getObjectsArrayElement(rowIdx);
    if ((desired == String.class || desired == Object.class) && tmp != null) {
      return enableStringCache ? cache.get(rowIdx, () -> new String((byte[])tmp, charset)) : new String((byte[])tmp, charset);
    }
    return tmp;
  }
}

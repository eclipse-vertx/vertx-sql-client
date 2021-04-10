/*
 *
 *  Copyright (c) 2021 Vladimir Vishnevsky
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

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PairedIterator<T> implements Iterator<Map.Entry<T, T>> {
  private final Iterator<T> wrapped1;
  private final Iterator<T> wrapped2;

  private PairedIterator(Iterator<T> wrapped1, Iterator<T> wrapped2) {
    this.wrapped1 = wrapped1;
    this.wrapped2 = wrapped2;
  }

  @Override
  public boolean hasNext() {
    return wrapped1.hasNext() && wrapped2.hasNext();
  }

  @Override
  public Map.Entry<T, T> next() {
    T key = wrapped1.next();
    T val = wrapped2.next();
    return new AbstractMap.SimpleEntry<>(key, val);
  }

  public static <T>  Iterator<Map.Entry<T, T>> of(List<T> src) {
    if (src.size() <= 1) {
      return Collections.emptyIterator();
    }

    Iterator<T> iter2 = src.iterator();
    iter2.next();
    return new PairedIterator<>(src.iterator(), iter2);
  }

  public static void main(String[] args) {
    Iterator<Map.Entry<String, String>> iter = PairedIterator.of(Arrays.asList("A", "B", "C"));
    while (iter.hasNext()) {
      Map.Entry<String, String> n = iter.next();
      System.err.println(n.getKey() + "; " + n.getValue());
    }
  }
}

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

package io.vertx.sqlclient.impl.accumulator;

import io.vertx.sqlclient.RowIterator;

import java.util.ArrayList;
import java.util.Iterator;

public class ArrayListAccumulator<T> extends ArrayList<T> implements Accumulator<T> {

  @Override
  public void accept(T t) {
    add(t);
  }

  @Override
  public RowIterator<T> iterator() {
    return rowIterator(super.iterator());
  }

  private static <U> RowIterator<U> rowIterator(Iterator<U> iter) {
    return new RowIterator<U>() {
      @Override
      public boolean hasNext() {
        return iter.hasNext();
      }

      @Override
      public U next() {
        return iter.next();
      }
    };
  }
}

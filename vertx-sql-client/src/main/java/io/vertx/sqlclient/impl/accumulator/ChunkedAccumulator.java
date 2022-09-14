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

import java.util.NoSuchElementException;
import java.util.function.IntUnaryOperator;

public class ChunkedAccumulator<T> implements Accumulator<T> {

  // How many items can be stored in the first chunk
  private static final int FIRST_CHUNK_CAPACITY = 10;

  private final IntUnaryOperator extensionPolicy;

  // Each chunk is an array with
  // - at most N value objects
  // - a last element pointing to the next chunk (if any)

  // Reference useful when iterating
  private final Object[] firstChunk;
  // Reference useful when adding
  private Object[] lastChunk;
  // Number of elements in the last chunk
  private int count;

  /**
   * @param extensionPolicy determines the capacity of a new chunk using the capacity of the previous one
   */
  public ChunkedAccumulator(IntUnaryOperator extensionPolicy) {
    this.extensionPolicy = extensionPolicy;
    firstChunk = lastChunk = newChunk(FIRST_CHUNK_CAPACITY);
    count = 0;
  }

  private Object[] newChunk(int capacity) {
    return new Object[capacity + 1];
  }

  @Override
  public void accept(T item) {
    int chunkCapacity = chunkCapacity(lastChunk);
    if (count == chunkCapacity) {
      Object[] chunk = newChunk(extensionPolicy.applyAsInt(chunkCapacity));
      chunk[0] = item;
      lastChunk[chunkCapacity] = chunk;
      lastChunk = chunk;
      count = 1;
    } else {
      lastChunk[count] = item;
      count++;
    }
  }

  private static int chunkCapacity(Object[] chunk) {
    return chunk.length - 1;
  }

  @Override
  public RowIterator<T> iterator() {
    return rowIterator(firstChunk, lastChunk, count);
  }

  private static <U> RowIterator<U> rowIterator(Object[] firstChunk, Object[] lastChunk, int count) {
    return new RowIterator<U>() {

      Object[] curr = firstChunk;
      int idx = 0;

      @Override
      public boolean hasNext() {
        if (curr != lastChunk) {
          int chunkCapacity = chunkCapacity(curr);
          return idx < chunkCapacity || curr[chunkCapacity] != null;
        } else {
          return idx < count;
        }
      }

      @Override
      public U next() {
        if (curr != lastChunk) {
          int chunkCapacity = chunkCapacity(curr);
          if (idx == chunkCapacity) {
            Object[] next = (Object[]) curr[chunkCapacity];
            if (next == null) {
              throw new NoSuchElementException();
            }
            curr = next;
            idx = 0;
          }
        } else if (idx == count) {
          throw new NoSuchElementException();
        }
        U item = value();
        idx++;
        return item;
      }

      @SuppressWarnings("unchecked")
      private U value() {
        return (U) curr[idx];
      }
    };
  }
}

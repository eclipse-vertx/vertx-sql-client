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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.*;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class RowAccumulatorTest {

  @Parameters(name = "Accumulate {index} item(s)")
  public static List<Object[]> testData() {
    return new AbstractList<Object[]>() {
      @Override
      public Object[] get(int index) {
        switch (index) {
          case 0:
            return new Object[]{Collections.emptyList()};
          case 1:
            return new Object[]{Collections.singletonList(UUID.randomUUID().toString())};
          default:
            return new Object[]{IntStream.range(0, index).mapToObj(i -> UUID.randomUUID().toString()).collect(toList())};
        }
      }

      @Override
      public int size() {
        return 1000;
      }
    };
  }

  private final List<String> data;

  public RowAccumulatorTest(List<String> data) {
    this.data = data;
  }

  @Test
  public void testArrayListAccumulator() {
    doTest(new ArrayListRowAccumulator<>());
  }

  private void doTest(RowAccumulator<String> rowAccumulator) {
    data.forEach(rowAccumulator);
    List<String> actual = new ArrayList<>(data.size());
    for (String value : rowAccumulator) {
      actual.add(value);
    }
    assertEquals(data, actual);
  }

  @Test
  public void testChunkedAccumulatorFixedChunkSize() {
    doTest(new ChunkedRowAccumulator<>(IntUnaryOperator.identity()));
  }

  @Test
  public void testChunkedAccumulatorGrowingChunkSize() {
    doTest(new ChunkedRowAccumulator<>(size -> size + (size >> 1)));
  }
}

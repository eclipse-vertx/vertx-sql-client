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

package io.reactiverse.pgclient;

import io.reactiverse.sqlclient.Tuple;
import org.junit.Test;

import static org.junit.Assert.*;

public class TupleTest {

  @Test
  public void testNumbers() {
    Tuple tuple = Tuple.of((byte)127, (short)4000, 1_000_000, 1_000_000_000L, 4.5F, 4.5D);
    assertEquals(127, (short)tuple.getShort(0));
    assertEquals(4000, (short)tuple.getShort(1));
    assertEquals(4, (short)tuple.getShort(4));
    assertEquals(4, (short)tuple.getShort(5));
    assertEquals(127, (int)tuple.getInteger(0));
    assertEquals(4000, (int)tuple.getInteger(1));
    assertEquals(1_000_000, (int)tuple.getInteger(2));
    assertEquals(1_000_000_000, (int)tuple.getInteger(3));
    assertEquals(4, (int)tuple.getInteger(4));
    assertEquals(4, (int)tuple.getInteger(5));
    assertEquals(127, (long)tuple.getLong(0));
    assertEquals(4000, (long)tuple.getLong(1));
    assertEquals(1_000_000, (long)tuple.getLong(2));
    assertEquals(1_000_000_000, (long)tuple.getLong(3));
    assertEquals(4, (long)tuple.getLong(4));
    assertEquals(4, (long)tuple.getLong(5));
    assertEquals(127, tuple.getFloat(0), 0.0f);
    assertEquals(4000, tuple.getFloat(1), 0.0f);
    assertEquals(1_000_000, tuple.getFloat(2), 0.0f);
    assertEquals(1_000_000_000, tuple.getFloat(3), 0.0f);
    assertEquals(4.5, tuple.getFloat(4), 0.0f);
    assertEquals(4.5, tuple.getFloat(5), 0.0f);
    assertEquals(127, tuple.getDouble(0), 0.0D);
    assertEquals(4000, tuple.getDouble(1), 0.0D);
    assertEquals(1_000_000, tuple.getDouble(2), 0.0D);
    assertEquals(1_000_000_000, tuple.getDouble(3), 0.0D);
    assertEquals(4.5, tuple.getDouble(4), 0.0D);
    assertEquals(4.5, tuple.getDouble(5), 0.0D);
  }
}

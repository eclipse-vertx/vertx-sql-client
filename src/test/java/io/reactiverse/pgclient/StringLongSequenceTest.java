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

import io.reactiverse.pgclient.impl.StringLongSequence;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class StringLongSequenceTest {

  @Test
  public void testSequence() {
    StringLongSequence seq = new StringLongSequence();
    assertEquals("0000000", seq.next());
    assertEquals("0000001", seq.next());
    assertEquals("0000002", seq.next());
    assertEquals("0000003", seq.next());
    assertEquals("0000004", seq.next());
    assertEquals("0000005", seq.next());
    assertEquals("0000006", seq.next());
    assertEquals("0000007", seq.next());
    assertEquals("0000008", seq.next());
    assertEquals("0000009", seq.next());
    assertEquals("000000A", seq.next());
    assertEquals("000000B", seq.next());
    assertEquals("000000C", seq.next());
    assertEquals("000000D", seq.next());
    assertEquals("000000E", seq.next());
    assertEquals("000000F", seq.next());
    assertEquals("0000010", seq.next());
  }

  @Test
  public void testEndingZero() {
    StringLongSequence seq = new StringLongSequence();
    for (int i = 0;i < 10000;i++) {
      Assert.assertEquals(0, seq.next() & 0xFF);
    }
  }

  private static void assertEquals(String s, long l) {
    ByteBuf buf = Unpooled.buffer();
    buf.writeLong(l);
    String actual = buf.getCharSequence(0, 7, StandardCharsets.UTF_8).toString();
    Assert.assertEquals(s, actual);
  }
}

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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.reactiverse.pgclient.impl.codec.util.UTF8StringEndDetector;
import io.reactiverse.pgclient.impl.codec.util.Util;
import io.vertx.core.buffer.Buffer;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class UtilTest {

  @Test
  public void testWriteHexString() {
    assertWriteHexString("00", (byte) 0);
    assertWriteHexString("01", (byte) 1);
    assertWriteHexString("0a", (byte) 10);
    assertWriteHexString("10", (byte) 16);
    assertWriteHexString("ff", (byte) 255);
    assertWriteHexString("ff0a0a", (byte) 255, (byte)10, (byte)10);
    Buffer buff = Buffer.buffer();
    for (int i = 0; i < 512;i++) {
      buff.appendByte((byte)('A' + i % 26));
    }
  }

  private static void assertWriteHexString(String expected, byte... data) {
    ByteBuf buff = Unpooled.buffer();
    Util.writeHexString(Buffer.buffer().appendBytes(data), buff);
    String hex = buff.toString(StandardCharsets.UTF_8);
    assertEquals(expected, hex);
  }

  @Test
  public void testUTF8StringEndDetector() throws Exception {
    assertSeparator("", -1);
    assertSeparator("\"", -1);
    assertSeparator("\"\"", 1);
    assertSeparator("\"a\"", 2);
    assertSeparator("\"€\"", 4);
    assertSeparator("\"\\\"\"", 3);
  }

  private void assertSeparator(String s, int expected) throws Exception {
    ByteBuf buf = Unpooled.buffer();
    buf.writeCharSequence(s, StandardCharsets.UTF_8);
    UTF8StringEndDetector processor = new UTF8StringEndDetector();
    int actual = buf.forEachByte(processor);
    assertEquals(expected, actual);
  }

}

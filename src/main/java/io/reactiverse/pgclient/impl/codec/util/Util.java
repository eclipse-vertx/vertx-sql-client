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

package io.reactiverse.pgclient.impl.codec.util;

import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;

import java.nio.charset.Charset;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.*;


public class Util {

  private static final byte ZERO = 0;

  public static String readCString(ByteBuf src, Charset charset) {
    int len = src.bytesBefore(ZERO);
    String s = src.readCharSequence(len, charset).toString();
    src.readByte();
    return s;
  }

  public static String readCStringUTF8(ByteBuf src) {
    int len = src.bytesBefore(ZERO);
    String s = src.readCharSequence(len, UTF_8).toString();
    src.readByte();
    return s;
  }

  public static void writeCString(ByteBuf dst, String s, Charset charset) {
    dst.writeCharSequence(s, charset);
    dst.writeByte(0);
  }

  public static void writeCString(ByteBuf dst, ByteBuf buf) {
    // Important : won't not change data index
    dst.writeBytes(buf, buf.readerIndex(), buf.readableBytes());
    dst.writeByte(0);
  }

  public static void writeCStringUTF8(ByteBuf dst, String s) {
    dst.writeCharSequence(s, UTF_8);
    dst.writeByte(0);
  }

  public static void writeCString(ByteBuf dst, byte[] bytes) {
    dst.writeBytes(bytes, 0, bytes.length);
    dst.writeByte(0);
  }

  public static String buildInvalidArgsError(Stream<Object> values, Stream<Class> types) {
    return "Values [" + values.map(String::valueOf).collect(Collectors.joining(", ")) +
      "] cannot be coerced to [" + types
      .map(Class::getSimpleName)
      .collect(Collectors.joining(", ")) + "]";
  }

  private static final int FIRST_HALF_BYTE_MASK = 0x0F;

  public static int writeHexString(Buffer buffer, ByteBuf to) {
    int len = buffer.length();
    for (int i = 0; i < len; i++) {
      final int b = Byte.toUnsignedInt(buffer.getByte(i));
      final int firstDigit = b >> 4;
      final byte firstHexDigit = (byte)bin2hex(firstDigit);
      final int secondDigit = b & FIRST_HALF_BYTE_MASK;
      final byte secondHexDigit = (byte)bin2hex(secondDigit);
      to.writeByte(firstHexDigit);
      to.writeByte(secondHexDigit);
    }
    return len;
  }

  private static int bin2hex(int digit){
    final int isLessOrEqual9 =(digit-10)>>31;
    //isLessOrEqual9==0xff<->digit<=9
    //bin2hexAsciiDistance=digit<=9?48:87;
    final int bin2hexAsciiDistance = 48+((~isLessOrEqual9)&39);
    return digit+bin2hexAsciiDistance;
  }
}

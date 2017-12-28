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

package com.julienviet.pgclient.impl.codec.util;

import com.julienviet.pgclient.impl.codec.DataType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.charset.Charset;
import java.util.List;
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

  public static void encodeBinaryArguments(ByteBuf out, DataType<?>[] dataTypes, List<Object> arguments) {
    if(arguments == null || arguments.isEmpty()) {
      // No parameter formats
      out.writeShort(0);
      // No parameter values
      out.writeShort(0);
    } else {

      // byte[][] foobar = Util.paramValues(paramValues);
      int len = arguments.size();
      out.writeShort(len);
      // Parameter formats
      for (int c = 0;c < len;c++) {
        // for now each format is Binary
        out.writeShort(1);
      }
      out.writeShort(len);
      for (int c = 0;c < len;c++) {
        Object param = arguments.get(c);
        if (param == null) {
          // NULL value
          out.writeInt(-1);
        } else {
          DataType dataType = dataTypes[c];
          dataType.binaryEncoder.encode(param, out);
        }
      }
    }
  }
}

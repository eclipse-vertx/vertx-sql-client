/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl.utils;

import io.netty.buffer.ByteBuf;

import static java.nio.charset.StandardCharsets.UTF_16LE;

public class ByteBufUtils {

  public static void writeByteLengthString(ByteBuf buffer, String value) {
    if (value == null) {
      buffer.writeByte(0);
    } else {
      buffer.writeByte(value.length());
      buffer.writeCharSequence(value, UTF_16LE);
    }
  }

  public static String readUnsignedByteLengthString(ByteBuf buffer) {
    int length = buffer.readUnsignedByte();
    return buffer.readCharSequence(length * 2, UTF_16LE).toString();
  }

  public static String readUnsignedShortLengthString(ByteBuf buffer) {
    int length = buffer.readUnsignedShortLE();
    return buffer.readCharSequence(length * 2, UTF_16LE).toString();
  }

  public static void writeUnsignedShortLengthString(ByteBuf buffer, String value) {
    buffer.writeShortLE(value.length() * 2);
    buffer.writeCharSequence(value, UTF_16LE);
  }

  public static long readUnsignedInt40LE(ByteBuf buffer) {
    long low = buffer.readUnsignedIntLE();
    short high = buffer.readUnsignedByte();
    return (0x100000000L * high) + low;
  }

  public static void writeUnsignedInt40LE(ByteBuf buffer, long value) {
    buffer.writeIntLE((int) (value % 0x100000000L));
    buffer.writeByte((int) (value / 0x100000000L));
  }

  private ByteBufUtils() {
    // Utility
  }
}

/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.sqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * This is based on this algorithm: https://lemire.me/blog/2022/01/21/swar-explained-parsing-eight-digits/
 * Which can be explained as follows:
 * <pre>
 *
 * Given 8 ASCII digits as: b1b2b3b4b5b6b7b8
 *
 * eg: "12345678"
 *
 * which byte[] := { 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38 }
 *                    "1"   "2"   "3"   "4"   "5"   "6"   "7"   "8"
 *
 * reading is in Little Endian form will read the lower indexes first placing them as lower addresses
 * as can be seen in the hex representation, where "1" is now in the rightmost position.
 *
 * hex:
 * 0x38_37_36_35_34_33_32_31-
 * 0x30_30_30_30_30_30_30_30=
 * 0x80_07_06_05_04_03_02_01
 *
 * digits = (digits * 10) + (digits >> 8);
 *
 * 0x50_46_3c_32_28_1e_14_0a +	~	digits * 10 +
 * 0x00_80_70_60_50_40_30_21 =	~	digits >> 8 =
 * 0x50_4e_43_38_2d_22_17_0c
 * now   ^     ^     ^     ^
 *       |     |	    |     |
 * g4=10*b7+b8 |     |     |
 *       g3=10*b5+b6 |     |
 *             g2=10*b3+b4 |
 *                   g1=10*b1+b2
 *
 * These are the relevant results we care about, while the others are useless
 * and subsequent masks will take care to ignore them.
 *
 * Now the aggregation parts:
 *
 * digits & U64_MASK := 0x00_00_00_38_00_00_00_0c
 *
 * This will isolate 10*b3+b4 and 10*b7+b8, trying to correctly compute:
 *
 * 1000000*(10*b1+b2) + 100*(10*b5+b6), somehow.
 *
 * The mask used to perform the multiplication is
 *
 * U64_FIRST_THIRD := (1000000L << 32) + 100 := 0x00_0f_42_40_00_00_00_64
 *
 * 0x00_00_00_38_00_00_00_0c *
 * 0x00_0f_42_40_00_00_00_64 =
 * 0x00_b7_30_e0_00_00_04_b0
 *
 *
 * which 0x00_b7_30_e0 part (let's ignore the second half 00_00_04_b0, which is 1200)
 *
 * is, in decimal:
 *
 * 12005600 (!!!) === 1000000*(10*1 + 2) + 100*(10*5 + 6) = 1*10000000 + 2*1000000 + 5*1000 + 6*100
 *
 * For the second part
 *
 * ie 10000*(10*b3 + b4) + 10*b7 + b8
 *
 * we first isolate the 2 paris (g2 and g4) with (digits >> 16) & U64_MASK (which move them to the right by 2 bytes), getting
 *
 * 0x00_00_4e_00_00_00_22 *
 * 0x00_27_10_00_00_00_01 =
 * 0x05_30_6e_00_00_00_22
 *
 * which, once again, has it leftmost part
 *
 * 0x05_30_6e === 340078 (!!!!) === 10000*(10*3 + 4) + 10*7 + 8 = 3*100000 + 4*10000 + 7*10 + 8
 *
 *
 * shifting both left by 32 and adding them, the total digit is done.
 * </pre>
 */
public class CommonCodec {

  // https://lemire.me/blog/2022/01/21/swar-explained-parsing-eight-digits/
  private static final long U64_MASK = 0x000000FF000000FFL;
  private static final long U64_FIRST_THIRD = (1000000L << 32) + 100;
  private static final long U64_SECOND_FOURTH = (10000L << 32) + 1;

  private static long parseEigthDigitsLE(long digits) {
    digits -= 0x3030303030303030L;
    digits = (digits * 10) + (digits >> 8);
    return (((digits & U64_MASK) * U64_FIRST_THIRD) + (((digits >> 16) & U64_MASK) * U64_SECOND_FOURTH)) >> 32;
  }

  private static final int U32_MASK = 0x00FF00FF;
  private static final int U32_FIRST_SECOND = (100 << 16) + 1;

  private static int parseFourDigitsLE(int digits) {
    digits -= 0x30303030;
    digits = (digits * 10) + (digits >> 8);
    return ((digits & U32_MASK) * U32_FIRST_SECOND) >> 16;
  }

  private static short parseTwoDigitsLE(short digits) {
    digits -= 0x3030;
    return (short) ((digits & 0xFF) * 10 + ((digits >> 8) & 0xFF));
  }

  private static byte parseOneDigit(byte digit) {
    return (byte) (digit - 0x30);
  }

  public static void main(String[] args) {
    ByteBuf buff = Unpooled.buffer();
    buff.writeCharSequence("-123", java.nio.charset.StandardCharsets.UTF_8);
    System.out.println(decodeDecStringToLong(0, buff.readableBytes(), buff));
  }

  public static int decodeDecStringToInt(int index, int len, ByteBuf buff) {
    // 10 + sign = 32bit
    return 0;
  }

  public static int decodeDecStringToShort(int index, int len, ByteBuf buff) {
    // 5 + sign = 16bit
    return 0;
  }

  public static long decodeDecStringToLong(int index, int len, ByteBuf buff) {
    byte firstByte = buff.getByte(index);
    final boolean negative = firstByte == '-';
    // handling these fast-path to avoid using get<something>LE which is not free
    if (len <= 2) {
      if (len == 1) {
        if (negative) {
          throw new IllegalArgumentException("Invalid negative number: missing digits");
        }
        return parseOneDigit(firstByte);
      }
      assert len == 2;
      if (negative) {
        return -parseOneDigit(buff.getByte(index + 1));
      }
      return parseOneDigit(firstByte) * 10 + parseOneDigit(buff.getByte(index + 1));
    }
    if (negative) {
      index++;
      len--;
    }
    long lessThanEight = len % 8;
    if (lessThanEight > 0) {
      return lessThanEightDigitsUnrolled(negative, index, len, buff);
    }
    throw new UnsupportedOperationException("Not implemented yet");
  }

  private static int lessThanEightDigitsUnrolled(boolean negative, int index, int len, ByteBuf buff) {
    assert len > 0 && len < 8;
    int digits = 0;
    int multiplier = 1;
    // len >= 4
    if ((len & Integer.BYTES) != 0) {
      digits = parseFourDigitsLE(buff.getIntLE(index));
      index += Integer.BYTES;
      multiplier = 100;
    }
    // len >= 2
    if ((len & Short.BYTES) != 0) {
      digits = digits * multiplier + parseTwoDigitsLE(buff.getShortLE(index));
      index += Short.BYTES;
      multiplier = 10;
    }
    // len >= 1
    if ((len & Byte.BYTES) != 0) {
      digits = digits * multiplier + parseOneDigit(buff.getByte(index));
    }
    return negative ? -digits : digits;
  }

  public static byte decodeDecStringToByte(int index, int len, ByteBuf buff) {
    // 3 + sign = 8bit
    return 0;
  }

}

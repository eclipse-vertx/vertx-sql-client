/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.sqlclient.impl;

/**
 * A sequence of hex values, each terminated by a zero byte.
 *
 * <p>The hex number is left padded to start with at least three 0
 * and to have at least seven digits.
 *
 * <p>After 000FFFFFFFFFFFFFFFF it will restart with 0000000.
 *
 * <p>The generated sequence:
 * <pre>
 * 0000000
 * 0000001
 * 0000002
 * ...
 * 000FFFF
 * 00010000
 * ...
 * 000FFFFF
 * 000100000
 * ...
 * 000FFFFFF
 * 0001000000
 * ...
 * 000FFFFFFFFFFFFFFFF
 * </pre>
 */
public class HexSequence {
  private long i;

  /**
   * Start the sequence with 0000000.
   */
  public HexSequence() {
    i = 0;
  }

  /**
   * @param startValue unsigned long for the first value returned by {@link #next()}
   */
  public HexSequence(long startValue) {
    i = startValue;
  }

  private static byte toHex(long c) {
    if (c < 10) {
      return (byte)('0' + c);
    } else {
      return (byte)('A' + c - 10);
    }
  }

  /**
   * A copy of the next hex value, terminated by a zero byte.
   */
  public byte[] next() {
    int len = 3  // 3 leading zeroes
        + (64 - Long.numberOfLeadingZeros(i) + 3) / 4  // hex characters
        + 1;  // tailing null byte
    len = Math.max(8, len);  // at least 7 hex digits plus null byte
    byte [] hex = new byte [len];
    int pos = len - 1;
    hex[pos--] = '\0';
    long n = i++;
    while (n != 0) {
      hex[pos--] = toHex(n & 0xf);
      n >>>= 4;
    }
    while (pos >= 0) {
      hex[pos--] = '0';
    }
    return hex;
  }
}

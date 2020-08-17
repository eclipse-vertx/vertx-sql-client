/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.sqlclient.impl;

import java.util.Arrays;
import java.util.Locale;

/**
 * A sequence of hex values, each terminated by a zero byte.
 *
 * The hex number is left padded to start with at least three 0
 * and to have at least seven digits.
 *
 * The generated sequence:
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
 * </pre>
 */
public class HexSequence {
  private byte[] hex;

  /**
   * Start the sequence with 0000000.
   */
  public HexSequence() {
    // seven 0 digits and a zero byte
    hex = new byte[] { '0', '0', '0', '0', '0', '0', '0', 0 };
  }

  /**
   * @param startValue first value returned by {@link #next()}.
   */
  public HexSequence(int startValue) {
    if (startValue < 0) {
      throw new IllegalArgumentException("startValue must not be negative: " + startValue);
    }
    hex = String.format(Locale.ROOT, "000%04X\0", startValue).getBytes();
  }

  /**
   * @return a copy of the current hex value, terminated by a zero byte.
   */
  public byte[] next() {
    byte[] ret = Arrays.copyOf(hex, hex.length);
    increment();
    return ret;
  }

  private void increment() {
    for (int i = hex.length - 2; i >= 3; i--) {
      byte number = next(hex[i]);
      hex[i] = number;
      if (number != '0') {
        return;
      }
    }
    hex = new byte [hex.length + 1];
    hex[0] = '0';
    hex[1] = '0';
    hex[2] = '0';
    hex[3] = '1';
    for (int i = 4; i < hex.length - 1; i++) {
      hex[i] = '0';
    }
    hex[hex.length - 1] = 0;
  }

  private byte next(byte c) {
    switch (c) {
    case '0': return '1';
    case '1': return '2';
    case '2': return '3';
    case '3': return '4';
    case '4': return '5';
    case '5': return '6';
    case '6': return '7';
    case '7': return '8';
    case '8': return '9';
    case '9': return 'A';
    case 'A': return 'B';
    case 'B': return 'C';
    case 'C': return 'D';
    case 'D': return 'E';
    case 'E': return 'F';
    default: return '0';
    }
  }
}

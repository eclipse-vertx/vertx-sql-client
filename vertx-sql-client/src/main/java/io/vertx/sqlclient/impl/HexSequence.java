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

import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * A sequence of hex values, each terminated by a zero byte.
 *
 * The hex number is left padded to start with at least three 0
 * and to have at least seven digits.
 *
 * After 000FFFFFFFFFFFFFFFF it will restart with 0000000.
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

  /**
   * @return a copy of the next hex value, terminated by a zero byte.
   */
  public byte[] next() {
    String hex = Long.toUnsignedString(i, 16).toUpperCase(Locale.ROOT);
    i++;
    switch (hex.length()) {
    case 1: return ("000000" + hex + "\0").getBytes(StandardCharsets.US_ASCII);
    case 2: return ("00000" + hex + "\0").getBytes(StandardCharsets.US_ASCII);
    case 3: return ("0000" + hex + "\0").getBytes(StandardCharsets.US_ASCII);
    default: return ("000" + hex + "\0").getBytes(StandardCharsets.US_ASCII);
    }
  }
}

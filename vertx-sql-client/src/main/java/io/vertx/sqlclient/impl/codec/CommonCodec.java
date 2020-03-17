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

public class CommonCodec {
  /**
   * Decode the specified {@code buff} formatted as a decimal string starting at the readable index
   * with the specified {@code length} to a long.
   *
   * @param index the hex string index
   * @param len   the hex string length
   * @param buff  the byte buff to read from
   * @return the decoded value as a long
   */
  public static long decodeDecStringToLong(int index, int len, ByteBuf buff) {
    long value = 0;
    if (len > 0) {
      int to = index + len;
      boolean neg = false;
      if (buff.getByte(index) == '-') {
        neg = true;
        index++;
      }
      while (index < to) {
        byte ch = buff.getByte(index++);
        byte nibble = (byte) (ch - '0');
        value = value * 10 + nibble;
      }
      if (neg) {
        value = -value;
      }
    }
    return value;
  }
}

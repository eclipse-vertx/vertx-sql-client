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

package io.reactiverse.pgclient.impl;

public class StringLongSequence {

  private short count;

  public long next() {
    short val = count++;
    long next = 0x30_30_30_00_00_00_00_00L;
    next |= toHex(val >> 12 & 0xF) << 32;
    next |= toHex(val >>  8 & 0xF) << 24;
    next |= toHex(val >>  4 & 0xF) << 16;
    next |= toHex(val >>  0 & 0xF) << 8;
    return next;
  }

  private static long toHex(int c) {
    if (c < 10) {
      return (byte)('0' + c);
    } else {
      return (byte)('A' + c - 10);
    }
  }

}

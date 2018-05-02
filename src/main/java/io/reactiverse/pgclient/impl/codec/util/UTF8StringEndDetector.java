/*
 * Copyright (C) 2018 Julien Viet
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

import io.netty.util.ByteProcessor;

/**
 * A processor that detects the end of a well formed UTF8 string, starting end ending with a {@code "}.
 * <p/>
 * It process all bytes until it finds the ending {@code "}.
 */
public class UTF8StringEndDetector implements ByteProcessor {

  private boolean inString;
  private boolean escaped;

  @Override
  public boolean process(byte value) {
    boolean wasEscaped = escaped;
    escaped = false;
    // In UTF-8 low ASCII have their 8th bit == 0
    if ((value & 0b10000000) == 0) {
      switch (value) {
        case '"':
          if (!wasEscaped) {
            if (inString) {
              return false;
            } else {
              inString = true;
            }
          }
          break;
        case '\\':
          if (inString) {
            escaped = true;
          }
          break;
      }
    }
    return true;
  }
}

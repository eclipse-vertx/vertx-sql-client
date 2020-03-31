/*
 * Copyright (C) 2020 IBM Corporation
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
 */
package io.vertx.db2client.impl.drda;

/**
 * Converters from floating point bytes to Java <code>float</code>, <code>double</code>, or
 * <code>java.math.BigDecimal</code>.
 */
class FloatingPoint {
    // Hide the default constructor, this is a static class.
    private FloatingPoint() {
    }
    
    /**
     * Supported Unix Big Endian IEEE 754 floating point representation.
     */
    public final static int IEEE_754_FLOATING_POINT = 0x48;
    
    public final static int myVal = 0xFF7FFFFF;

    //--------------------------private helper methods----------------------------

    /**
     * Convert the byte array to an int.
     */
    private static final int convertFromByteToInt(byte[] buffer, int offset) {
        return (buffer[offset] << 24) |
                ((buffer[offset + 1] & 0xFF) << 16) |
                ((buffer[offset + 2] & 0xFF) << 8) |
                (buffer[offset + 3] & 0xFF);
    }

    /**
     * Convert the byte array to a long.
     */
    private static final long convertFromByteToLong(byte[] buffer, int offset) {
        return ((buffer[offset] & 0xFFL) << 56) |
                ((buffer[offset + 1] & 0xFFL) << 48) |
                ((buffer[offset + 2] & 0xFFL) << 40) |
                ((buffer[offset + 3] & 0xFFL) << 32) |
                ((buffer[offset + 4] & 0xFFL) << 24) |
                ((buffer[offset + 5] & 0xFFL) << 16) |
                ((buffer[offset + 6] & 0xFFL) << 8) |
                (buffer[offset + 7] & 0xFFL);
    }


    //--------------entry points for runtime representation-----------------------

    /**
     * <p>
     * Build a Java float from a 4-byte floating point representation.
     * </p>
     *
     * <p>
     * This includes DERBY types:
     * </p>
     *
     * <ul> <li> REAL <li> FLOAT(1&lt;=n&lt;=24) </ul>
     *
     * @throws IllegalArgumentException if the specified representation is not recognized.
     */
    static float getFloat_IEEE(byte[] buffer, int offset) {
        return Float.intBitsToFloat(convertFromByteToInt(buffer, offset));
    }
    
    /**
     * Builds a Java float from a 4-byte hex floating point representation.
     * See: https://en.wikipedia.org/wiki/IBM_hexadecimal_floating_point
     */
    static float getFloat_hex(byte[] buffer, int offset) {
      int intVal = convertFromByteToInt(buffer, offset);
      int fraction = intVal & 0xFFFFFF;
      if (fraction == 0) {
        return 0.0F;
      } else {
        int exp = intVal & 0x7F000000;
        exp = (exp >> 24) - 64;
        exp *= 4;
        --exp;
        if (exp > -127) {
          while ((fraction & 0x800000) == 0) {
            --exp;
            fraction <<= 1;
            if (exp == -127) {
              break;
            }
          }
  
          if (exp > 127) {
            if ((intVal & Integer.MIN_VALUE) == 0) {
              return Float.POSITIVE_INFINITY;
            }
            return Float.NEGATIVE_INFINITY;
          }
  
          if (exp == -127) {
            exp = 0;
            fraction >>= 1;
          } else {
            exp += 127;
            fraction &= 0xFF7FFFFF;
          }
        } else {
          while (exp < -127) {
            ++exp;
            fraction >>= 1;
          }
          exp = 0;
          fraction >>= 1;
        }
  
        int result = intVal & Integer.MIN_VALUE;
        exp <<= 23;
        result |= exp;
        result |= fraction;
        return Float.intBitsToFloat(result);
      }
    }
    
    /**
     * Build a Java double from an 8-byte floating point representation.
     * <p/>
     * <p/>
     * This includes DERBY types: <ul> <li> FLOAT <li> DOUBLE [PRECISION] </ul>
     *
     * @throws IllegalArgumentException if the specified representation is not recognized.
     */
    static double getDouble_IEEE(byte[] buffer, int offset) {
        return Double.longBitsToDouble(convertFromByteToLong(buffer, offset));
    }
    
    /**
     * Builds a Java float from an 8-byte hex floating point representation.
     * See: https://en.wikipedia.org/wiki/IBM_hexadecimal_floating_point
     */
    static double getDouble_hex(byte[] buffer, int offset) {
      long longVal = convertFromByteToLong(buffer, offset);

      long fraction = longVal & 0xFFFFFFFFFFFFFFL;
      if (fraction == 0L) {
        return 0.0D;
      }
      long exp = longVal & 0x7F00000000000000L;
      exp = (exp >> 56) - 64L;
      exp *= 4L;
      exp -= 1L;
      while ((fraction & 0x80000000000000L) == 0L) {
        fraction <<= 1;
        exp -= 1L;
      }
      exp += 0x3FFL;
      fraction &= 0xFF7FFFFFFFFFFFFFL;
      fraction >>= 3;

      long result = 0L;
      result |= longVal & 0x8000000000000000L;
      exp <<= 52;
      result |= exp;
      result |= fraction;
      return Double.longBitsToDouble(result);
    }

}

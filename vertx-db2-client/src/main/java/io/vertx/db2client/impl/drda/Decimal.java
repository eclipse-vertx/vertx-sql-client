/*
 * Copyright (C) 2019,2020 IBM Corporation
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

import java.math.BigDecimal;
import java.math.BigInteger;

import io.netty.buffer.ByteBuf;

/**
 * Converters from fixed point decimal bytes to <code>java.math.BigDecimal</code>, <code>double</code>, or
 * <code>long</code>.
 */
public class Decimal {
    /**
     * Packed Decimal representation
     */
    public final static int PACKED_DECIMAL = 0x30;
    
    //--------------------------private constants---------------------------------

    private static final int[][] tenRadixMagnitude = {
        {0x3b9aca00}, // 10^9
        {0x0de0b6b3, 0xa7640000}, // 10^18
        {0x033b2e3c, 0x9fd0803c, 0xe8000000}, // 10^27
    };
    
    // Used only by computeBigDecimalPrecision()
    // byte array of 1, 10, 100, 1000, 10000, ..., 10^31 for
    // fast computing the length a BigDecimal.
    private static byte[][] tenRadixArr = {
        {(byte) 0x01}, // 10^0
        {(byte) 0x0A}, // 10^1
        {(byte) 0x64}, // 10^2
        {(byte) 0x03, (byte) 0xe8}, // 10^3
        {(byte) 0x27, (byte) 0x10}, // 10^4
        {(byte) 0x01, (byte) 0x86, (byte) 0xa0}, // 10^5
        {(byte) 0x0f, (byte) 0x42, (byte) 0x40}, // 10^6
        {(byte) 0x98, (byte) 0x96, (byte) 0x80}, // 10^7
        {(byte) 0x05, (byte) 0xf5, (byte) 0xe1, (byte) 0x00}, // 10^8
        {(byte) 0x3b, (byte) 0x9a, (byte) 0xca, (byte) 0x00}, // 10^9
        {(byte) 0x02, (byte) 0x54, (byte) 0x0b, (byte) 0xe4, (byte) 0x00}, // 10^10
        {(byte) 0x17, (byte) 0x48, (byte) 0x76, (byte) 0xe8, (byte) 0x00}, // 10^11
        {(byte) 0xe8, (byte) 0xd4, (byte) 0xa5, (byte) 0x10, (byte) 0x00}, // 10^12
        {(byte) 0x09, (byte) 0x18, (byte) 0x4e, (byte) 0x72, (byte) 0xa0, (byte) 0x00}, // 10^13
        {(byte) 0x5a, (byte) 0xf3, (byte) 0x10, (byte) 0x7a, (byte) 0x40, (byte) 0x00}, // 10^14
        {(byte) 0x03, (byte) 0x8d, (byte) 0x7e, (byte) 0xa4, (byte) 0xc6, (byte) 0x80, (byte) 0x00}, // 10^15
        {(byte) 0x23, (byte) 0x86, (byte) 0xf2, (byte) 0x6f, (byte) 0xc1, (byte) 0x00, (byte) 0x00}, // 10^16
        {(byte) 0x01, (byte) 0x63, (byte) 0x45, (byte) 0x78, (byte) 0x5d, (byte) 0x8a, (byte) 0x00, (byte) 0x00}, // 10^17
        {(byte) 0x0d, (byte) 0xe0, (byte) 0xb6, (byte) 0xb3, (byte) 0xa7, (byte) 0x64, (byte) 0x00, (byte) 0x00}, // 10^18
        {(byte) 0x8a, (byte) 0xc7, (byte) 0x23, (byte) 0x04, (byte) 0x89, (byte) 0xe8, (byte) 0x00, (byte) 0x00}, // 10^19
        {(byte) 0x05, (byte) 0x6b, (byte) 0xc7, (byte) 0x5e, (byte) 0x2d, (byte) 0x63, (byte) 0x10, (byte) 0x00, (byte) 0x00}, // 10^20
        {(byte) 0x36, (byte) 0x35, (byte) 0xc9, (byte) 0xad, (byte) 0xc5, (byte) 0xde, (byte) 0xa0, (byte) 0x00, (byte) 0x00}, // 10^21
        {(byte) 0x02, (byte) 0x1e, (byte) 0x19, (byte) 0xe0, (byte) 0xc9, (byte) 0xba, (byte) 0xb2, (byte) 0x40, (byte) 0x00, (byte) 0x00}, // 10^22
        {(byte) 0x15, (byte) 0x2d, (byte) 0x02, (byte) 0xc7, (byte) 0xe1, (byte) 0x4a, (byte) 0xf6, (byte) 0x80, (byte) 0x00, (byte) 0x00}, // 10^23
        {(byte) 0xd3, (byte) 0xc2, (byte) 0x1b, (byte) 0xce, (byte) 0xcc, (byte) 0xed, (byte) 0xa1, (byte) 0x00, (byte) 0x00, (byte) 0x00}, // 10^24
        {(byte) 0x08, (byte) 0x45, (byte) 0x95, (byte) 0x16, (byte) 0x14, (byte) 0x01, (byte) 0x48, (byte) 0x4a, (byte) 0x00, (byte) 0x00, (byte) 0x00}, // 10^25
        {(byte) 0x52, (byte) 0xb7, (byte) 0xd2, (byte) 0xdc, (byte) 0xc8, (byte) 0x0c, (byte) 0xd2, (byte) 0xe4, (byte) 0x00, (byte) 0x00, (byte) 0x00}, // 10^26
        {(byte) 0x03, (byte) 0x3b, (byte) 0x2e, (byte) 0x3c, (byte) 0x9f, (byte) 0xd0, (byte) 0x80, (byte) 0x3c, (byte) 0xe8, (byte) 0x00, (byte) 0x00, (byte) 0x00}, // 10^27
        {(byte) 0x20, (byte) 0x4f, (byte) 0xce, (byte) 0x5e, (byte) 0x3e, (byte) 0x25, (byte) 0x02, (byte) 0x61, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00}, // 10^28
        {(byte) 0x01, (byte) 0x43, (byte) 0x1e, (byte) 0x0f, (byte) 0xae, (byte) 0x6d, (byte) 0x72, (byte) 0x17, (byte) 0xca, (byte) 0xa0, (byte) 0x00, (byte) 0x00, (byte) 0x00}, // 10^29
        {(byte) 0x0c, (byte) 0x9f, (byte) 0x2c, (byte) 0x9c, (byte) 0xd0, (byte) 0x46, (byte) 0x74, (byte) 0xed, (byte) 0xea, (byte) 0x40, (byte) 0x00, (byte) 0x00, (byte) 0x00}, // 10^30
        {(byte) 0x7e, (byte) 0x37, (byte) 0xbe, (byte) 0x20, (byte) 0x22, (byte) 0xc0, (byte) 0x91, (byte) 0x4b, (byte) 0x26, (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00}  // 10^31
    };

    //--------------------------constructors--------------------------------------

    // Hide the default constructor, this is a static class.
    private Decimal() {
    }

    //--------------------------private helper methods----------------------------

    /**
     * Convert a range of packed nybbles (up to 9 digits without overflow) to an int. Note that for performance purpose,
     * it does not do array-out-of-bound checking.
     */
    private static final int packedNybblesToInt(ByteBuf buffer,
                                                int offset,
                                                int startNybble,
                                                int numberOfNybbles) {
        int value = 0;

        int i = startNybble / 2;
        if ((startNybble % 2) != 0) {
            // process low nybble of the first byte if necessary.
            value += buffer.getByte(offset + i) & 0x0F;
            i++;
        }

        int endNybble = startNybble + numberOfNybbles - 1;
        for (; i < (endNybble + 1) / 2; i++) {
            value = value * 10 + ((buffer.getByte(offset + i) & 0xF0) >>> 4); // high nybble.
            value = value * 10 + (buffer.getByte(offset + i) & 0x0F);        // low nybble.
        }

        if ((endNybble % 2) == 0) {
            // process high nybble of the last byte if necessary.
            value = value * 10 + ((buffer.getByte(offset + i) & 0xF0) >>> 4);
        }

        return value;
    }

    /**
     * Convert a range of packed nybbles (up to 18 digits without overflow) to a long. Note that for performance
     * purpose, it does not do array-out-of-bound checking.
     */
    private static final long packedNybblesToLong(ByteBuf buffer,
                                                  int offset,
                                                  int startNybble,
                                                  int numberOfNybbles) {
        long value = 0;

        int i = startNybble / 2;
        if ((startNybble % 2) != 0) {
            // process low nybble of the first byte if necessary.
            value += buffer.getByte(offset + i) & 0x0F;
            i++;
        }

        int endNybble = startNybble + numberOfNybbles - 1;
        for (; i < (endNybble + 1) / 2; i++) {
            value = value * 10 + ((buffer.getByte(offset + i) & 0xF0) >>> 4); // high nybble.
            value = value * 10 + (buffer.getByte(offset + i) & 0x0F);        // low nybble.
        }

        if ((endNybble % 2) == 0) {
            // process high nybble of the last byte if necessary.
            value = value * 10 + ((buffer.getByte(offset + i) & 0xF0) >>> 4);
        }

        return value;
    }

    /**
     * Compute the int array of magnitude from input value segments.
     */
    private static final int[] computeMagnitude(int[] input) {
        int length = input.length;
        int[] mag = new int[length];

        mag[length - 1] = input[length - 1];
        for (int i = 0; i < length - 1; i++) {
            int carry = 0;
            int j = tenRadixMagnitude[i].length - 1;
            int k = length - 1;
            for (; j >= 0; j--, k--) {
                long product = (input[length - 2 - i] & 0xFFFFFFFFL) * (tenRadixMagnitude[i][j] & 0xFFFFFFFFL)
                        + (mag[k] & 0xFFFFFFFFL) // add previous value
                        + (carry & 0xFFFFFFFFL); // add carry
                carry = (int) (product >>> 32);
                mag[k] = (int) (product & 0xFFFFFFFFL);
            }
            mag[k] = (int) carry;
        }
        return mag;
    }
    
    static public int computeBigDecimalPrecision(BigDecimal decimal) {
        byte[] bBytes = decimal.unscaledValue().abs().toByteArray();

        if (byteArrayCmp(bBytes, tenRadixArr[tenRadixArr.length - 1]) >= 0) {
            throw new IllegalArgumentException("CONN_PRECISION_TOO_LARGE");
        }

        int lo = 0, hi = tenRadixArr.length - 1, mi = (hi + lo) / 2;
        do {
            int compare = byteArrayCmp(bBytes, tenRadixArr[mi]);
            if (compare == 1) {
                lo = mi;
            } else if (compare == -1) {
                hi = mi;
            } else {
                break;
            }

            mi = (hi + lo) / 2;
        } while (mi != lo);

        return (mi + 1);
    }
    
    // Used only by computeBigDecimalPrecision()
    private static int byteArrayCmp(byte[] arg1, byte[] arg2) {
        int arg1Offset = 0;
        int arg2Offset = 0;
        int length;
        if (arg1.length > arg2.length) {
            int diff = arg1.length - arg2.length;
            for (; arg1Offset < diff; arg1Offset++) {
                if (arg1[arg1Offset] != 0) {
                    return 1;
                }
            }
            length = arg2.length;
        } else if (arg1.length < arg2.length) {
            int diff = arg2.length - arg1.length;
            for (; arg2Offset < diff; arg2Offset++) {
                if (arg2[arg2Offset] != 0) {
                    return -1;
                }
            }
            length = arg1.length;
        } else {
            length = arg1.length;
        }

        for (int i = 0; i < length; i++) {
            int b1 = arg1[arg1Offset + i] & 0xFF;
            int b2 = arg2[arg2Offset + i] & 0xFF;
            if (b1 > b2) {
                return 1;
            } else if (b1 < b2) {
                return -1;
            }
        }
        return 0;
    }

    //--------------entry points for runtime representation-----------------------

    /**
     * Build a <code>java.math.BigDecimal</code> from a fixed point decimal byte representation.
     *
     * @throws IllegalArgumentException if the specified representation is not recognized.
     */
    static BigDecimal getBigDecimal(
            ByteBuf buffer,
            int offset,
            int precision,
            int scale) {

        // The byte-length of a packed decimal with precision <code>p</code> is always <code>p/2 + 1</code>
        int length = precision / 2 + 1;

        // check for sign.
        int signum;
        if ((buffer.getByte(offset + length - 1) & 0x0F) == 0x0D) {
        //if ((buffer[offset + length - 1] & 0x0F) == 0x0D) {
            signum = -1;
        } else {
            signum = 1;
        }

        if (precision <= 18) {
            // can be handled by long without overflow.
            long value = packedNybblesToLong(buffer, offset, 0, length * 2 - 1);
            if (signum < 0) {
                value = -value;
            }
            return BigDecimal.valueOf(value, scale);
        } else if (precision <= 27) {
            // get the value of last 9 digits (5 bytes).
            int lo = packedNybblesToInt(buffer, offset, (length - 5) * 2, 9);
            // get the value of another 9 digits (5 bytes).
            int me = packedNybblesToInt(buffer, offset, (length - 10) * 2 + 1, 9);
            // get the value of the rest digits.
            int hi = packedNybblesToInt(buffer, offset, 0, (length - 10) * 2 + 1);

            // compute the int array of magnitude.
            int[] value = computeMagnitude(new int[]{hi, me, lo});

            // convert value to a byte array of magnitude.
            byte[] magnitude = new byte[12];
            magnitude[0] = (byte) (value[0] >>> 24);
            magnitude[1] = (byte) (value[0] >>> 16);
            magnitude[2] = (byte) (value[0] >>> 8);
            magnitude[3] = (byte) (value[0]);
            magnitude[4] = (byte) (value[1] >>> 24);
            magnitude[5] = (byte) (value[1] >>> 16);
            magnitude[6] = (byte) (value[1] >>> 8);
            magnitude[7] = (byte) (value[1]);
            magnitude[8] = (byte) (value[2] >>> 24);
            magnitude[9] = (byte) (value[2] >>> 16);
            magnitude[10] = (byte) (value[2] >>> 8);
            magnitude[11] = (byte) (value[2]);

            return new BigDecimal(new BigInteger(signum, magnitude), scale);
        } else if (precision <= 31) {
            // get the value of last 9 digits (5 bytes).
            int lo = packedNybblesToInt(buffer, offset, (length - 5) * 2, 9);
            // get the value of another 9 digits (5 bytes).
            int meLo = packedNybblesToInt(buffer, offset, (length - 10) * 2 + 1, 9);
            // get the value of another 9 digits (5 bytes).
            int meHi = packedNybblesToInt(buffer, offset, (length - 14) * 2, 9);
            // get the value of the rest digits.
            int hi = packedNybblesToInt(buffer, offset, 0, (length - 14) * 2);

            // compute the int array of magnitude.
            int[] value = computeMagnitude(new int[]{hi, meHi, meLo, lo});

            // convert value to a byte array of magnitude.
            byte[] magnitude = new byte[16];
            magnitude[0] = (byte) (value[0] >>> 24);
            magnitude[1] = (byte) (value[0] >>> 16);
            magnitude[2] = (byte) (value[0] >>> 8);
            magnitude[3] = (byte) (value[0]);
            magnitude[4] = (byte) (value[1] >>> 24);
            magnitude[5] = (byte) (value[1] >>> 16);
            magnitude[6] = (byte) (value[1] >>> 8);
            magnitude[7] = (byte) (value[1]);
            magnitude[8] = (byte) (value[2] >>> 24);
            magnitude[9] = (byte) (value[2] >>> 16);
            magnitude[10] = (byte) (value[2] >>> 8);
            magnitude[11] = (byte) (value[2]);
            magnitude[12] = (byte) (value[3] >>> 24);
            magnitude[13] = (byte) (value[3] >>> 16);
            magnitude[14] = (byte) (value[3] >>> 8);
            magnitude[15] = (byte) (value[3]);

            return new BigDecimal(new BigInteger(signum, magnitude), scale);
        } else {
            // throw an exception here if nibbles is greater than 31
            throw new IllegalArgumentException("SQLState.DECIMAL_TOO_MANY_DIGITS");
        }
    }

    /**
     * Build a Java <code>double</code> from a fixed point decimal byte representation.
     *
     * @throws IllegalArgumentException if the specified representation is not recognized.
     */
    static double getDouble(
            ByteBuf buffer,
            int offset,
            int precision,
            int scale) {

        // The byte-length of a packed decimal with precision <code>p</code> is always <code>p/2 + 1</code>
        int length = precision / 2 + 1;

        // check for sign.
        int signum;
        //if ((buffer[offset + length - 1] & 0x0F) == 0x0D) {
        if ((buffer.getByte(offset + length - 1) & 0x0F) == 0x0D) {
            signum = -1;
        } else {
            signum = 1;
        }

        if (precision <= 9) {
            // can be handled by int without overflow.
            int value = packedNybblesToInt(buffer, offset, 0, length * 2 - 1);

            return signum * value / Math.pow(10, scale);
        } else if (precision <= 18) {
            // can be handled by long without overflow.
            long value = packedNybblesToLong(buffer, offset, 0, length * 2 - 1);

            return signum * value / Math.pow(10, scale);
        } else if (precision <= 27) {
            // get the value of last 9 digits (5 bytes).
            int lo = packedNybblesToInt(buffer, offset, (length - 5) * 2, 9);
            // get the value of another 9 digits (5 bytes).
            int me = packedNybblesToInt(buffer, offset, (length - 10) * 2 + 1, 9);
            // get the value of the rest digits.
            int hi = packedNybblesToInt(buffer, offset, 0, (length - 10) * 2 + 1);

            return signum * (lo / Math.pow(10, scale) +
                    me * Math.pow(10, 9 - scale) +
                    hi * Math.pow(10, 18 - scale));
        } else if (precision <= 31) {
            // get the value of last 9 digits (5 bytes).
            int lo = packedNybblesToInt(buffer, offset, (length - 5) * 2, 9);
            // get the value of another 9 digits (5 bytes).
            int meLo = packedNybblesToInt(buffer, offset, (length - 10) * 2 + 1, 9);
            // get the value of another 9 digits (5 bytes).
            int meHi = packedNybblesToInt(buffer, offset, (length - 14) * 2, 9);
            // get the value of the rest digits.
            int hi = packedNybblesToInt(buffer, offset, 0, (length - 14) * 2);

            return signum * (lo / Math.pow(10, scale) +
                    meLo * Math.pow(10, 9 - scale) +
                    meHi * Math.pow(10, 18 - scale) +
                    hi * Math.pow(10, 27 - scale));
        } else {
            // throw an exception here if nibbles is greater than 31
            throw new IllegalArgumentException("SQLState.DECIMAL_TOO_MANY_DIGITS");
        }
    }

    /**
     * Build a Java <code>long</code> from a fixed point decimal byte representation.
     *
     * @throws IllegalArgumentException if the specified representation is not recognized.
     * @throws ArithmeticException if value is too large for a long
     */
    static long getLong(
            ByteBuf buffer,
            int offset,
            int precision,
            int scale) {

        if (precision > 31) {
            // throw an exception here if nibbles is greater than 31
            throw new IllegalArgumentException("SQLState.DECIMAL_TOO_MANY_DIGITS");
        }

        // The byte-length of a packed decimal with precision <code>p</code> is always <code>p/2 + 1</code>
        int length = precision / 2 + 1;

        // check for sign.
        int signum;
        //if ((buffer[offset + length - 1] & 0x0F) == 0x0D) {
        if ((buffer.getByte(offset + length - 1) & 0x0F) == 0x0D) {
            signum = -1;
        } else {
            signum = 1;
        }

        if (precision - scale <= 18) {
            // Can be handled by long without overflow.
            // Compute the integer part only.
            int leftOfDecimalPoint = length * 2 - 1 - scale;
            return signum * packedNybblesToLong(buffer, offset, 0,
                                                leftOfDecimalPoint);
        } else {
            // Strip off fraction part by converting via BigInteger
            // lest longValueExact will throw ArithmeticException
            BigDecimal tmp = new BigDecimal(
                getBigDecimal(buffer, offset, precision, scale).toBigInteger());
            // throws ArithmeticException if overflow:
            return tmp.longValueExact();
        }
    }

    //--------------entry points for runtime representation-----------------------

    /**
     * Write a Java <code>java.math.BigDecimal</code> to packed decimal bytes.
     */
    public static final int bigDecimalToPackedDecimalBytes(ByteBuf buffer,
                                                           int offset,
                                                           BigDecimal b,
                                                           int declaredPrecision,
                                                           int declaredScale) {
        // packed decimal may only be up to 31 digits.
        if (declaredPrecision > 31) {
            throw new IllegalArgumentException("SQLState.DECIMAL_TOO_MANY_DIGITS " + declaredPrecision);
        }

        // get absolute unscaled value of the BigDecimal as a String.
        String unscaledStr = b.unscaledValue().abs().toString();

        // get precision of the BigDecimal.
        int bigPrecision = unscaledStr.length();

        if (bigPrecision > 31) {
            throw new IllegalArgumentException("SQLState.LANG_OUTSIDE_RANGE_FOR_DATATYPE " + new SqlCode(-405) + "packed decimal");
        }

        int bigScale = b.scale();
        int bigWholeIntegerLength = bigPrecision - bigScale;
        if ((bigWholeIntegerLength > 0) && (!unscaledStr.equals("0"))) {
            // if whole integer part exists, check if overflow.
            int declaredWholeIntegerLength = declaredPrecision - declaredScale;
            if (bigWholeIntegerLength > declaredWholeIntegerLength) {
                throw new IllegalArgumentException("SQLState.NUMERIC_OVERFLOW " + new SqlCode(-413) + b.toString() + "packed decimal");
            }
        }

        // convert the unscaled value to a packed decimal bytes.

        // get unicode '0' value.
        int zeroBase = '0';

        // start index in target packed decimal.
        int packedIndex = declaredPrecision - 1;

        // start index in source big decimal.
        int bigIndex;

        if (bigScale >= declaredScale) {
            // If target scale is less than source scale,
            // discard excessive fraction.

            // set start index in source big decimal to ignore excessive fraction.
            bigIndex = bigPrecision - 1 - (bigScale - declaredScale);

            if (bigIndex < 0) {
                // all digits are discarded, so only process the sign nybble.
                buffer.setByte(offset + (packedIndex + 1) / 2, (byte) ((b.signum() >= 0) ? 12 : 13)); // sign nybble
            } else {
                // process the last nybble together with the sign nybble.
                buffer.setByte(offset + (packedIndex + 1) / 2,
                        (byte) (((unscaledStr.charAt(bigIndex) - zeroBase) << 4) + // last nybble
                        ((b.signum() >= 0) ? 12 : 13))); // sign nybble
            }
            packedIndex -= 2;
            bigIndex -= 2;
        } else {
            // If target scale is greater than source scale,
            // pad the fraction with zero.

            // set start index in source big decimal to pad fraction with zero.
            bigIndex = declaredScale - bigScale - 1;

            // process the sign nybble.
            buffer.setByte(offset + (packedIndex + 1) / 2,
                    (byte) ((b.signum() >= 0) ? 12 : 13)); // sign nybble

            for (packedIndex -= 2, bigIndex -= 2; bigIndex >= 0; packedIndex -= 2, bigIndex -= 2) {
                buffer.setByte(offset + (packedIndex + 1) / 2, (byte) 0);
            }

            if (bigIndex == -1) {
                buffer.setByte(offset + (packedIndex + 1) / 2,
                        (byte) ((unscaledStr.charAt(bigPrecision - 1) - zeroBase) << 4)); // high nybble

                packedIndex -= 2;
                bigIndex = bigPrecision - 3;
            } else {
                bigIndex = bigPrecision - 2;
            }
        }

        // process the rest.
        for (; bigIndex >= 0; packedIndex -= 2, bigIndex -= 2) {
            buffer.setByte(offset + (packedIndex + 1) / 2,
                    (byte) (((unscaledStr.charAt(bigIndex) - zeroBase) << 4) + // high nybble
                    (unscaledStr.charAt(bigIndex + 1) - zeroBase))); // low nybble
        }

        // process the first nybble when there is one left.
        if (bigIndex == -1) {
            buffer.setByte(offset + (packedIndex + 1) / 2,
                    (byte) (unscaledStr.charAt(0) - zeroBase));

            packedIndex -= 2;
        }

        // pad zero in front of the big decimal if necessary.
        for (; packedIndex >= -1; packedIndex -= 2) {
            buffer.setByte(offset + (packedIndex + 1) / 2, (byte) 0);
        }

        return declaredPrecision / 2 + 1;
    }
}

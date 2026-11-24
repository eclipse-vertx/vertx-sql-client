/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.sqlclient.data;

import java.util.Arrays;
import java.util.Objects;

/**
 * A vector of bits, packed most significant bit first.
 * <p>
 * Instances map bit-packed vector types, e.g Oracle {@code VECTOR(n, BINARY)} columns, and
 * the {@code bit}/{@code varbit} types of PostgreSQL (the latter through the
 * {@code Row#get(BitVector.class, ...)} conversion).
 * <p>
 * Instances have value semantics: {@link #equals(Object)} and {@link #hashCode()} compare the
 * bit length and the packed bytes, regardless of the representation an instance was created
 * from.
 */
public final class BitVector implements Vector {

  private final byte[] packedBits;
  private final int bitLength;

  /**
   * Create a {@code BitVector} from its bits, packed most significant bit first.
   *
   * @param bits the bits
   * @return the vector
   * @throws NullPointerException when {@code bits} is {@code null}
   */
  public static BitVector create(boolean[] bits) {
    Objects.requireNonNull(bits, "bits cannot be null");
    int bitLength = bits.length;
    byte[] packedBits = new byte[(bitLength + 7) / 8];
    for (int i = 0; i < bitLength; i++) {
      if (bits[i]) {
        packedBits[i / 8] |= (byte) (0x80 >> (i % 8));
      }
    }
    return new BitVector(packedBits, bitLength);
  }

  /**
   * Create a {@code BitVector} from bits packed most significant bit first.
   * <p>
   * The given array is stored without copy, mutations are visible.
   *
   * @param packedBits the packed bits, of length {@code (bitLength + 7) / 8}
   * @param bitLength the number of significant bits
   * @return the vector
   * @throws NullPointerException when {@code packedBits} is {@code null}
   * @throws IllegalArgumentException when {@code bitLength} is negative or does not match the
   * length of {@code packedBits}
   */
  public static BitVector create(byte[] packedBits, int bitLength) {
    Objects.requireNonNull(packedBits, "packedBits cannot be null");
    if (bitLength < 0) {
      throw new IllegalArgumentException("bitLength must be non-negative: " + bitLength);
    }
    if (packedBits.length != (bitLength + 7) / 8) {
      throw new IllegalArgumentException("packedBits length must be (bitLength + 7) / 8: expected "
        + ((bitLength + 7) / 8) + " but was " + packedBits.length);
    }
    return new BitVector(packedBits, bitLength);
  }

  /**
   * Create a {@code BitVector} from its text representation, e.g {@code "10110100"}.
   *
   * @param binaryDigits the text representation, made of {@code '0'} and {@code '1'} digits
   * @return the vector
   * @throws NullPointerException when {@code binaryDigits} is {@code null}
   * @throws IllegalArgumentException when {@code binaryDigits} contains characters other than
   * {@code '0'} and {@code '1'}
   */
  public static BitVector create(String binaryDigits) {
    Objects.requireNonNull(binaryDigits, "binaryDigits cannot be null");
    int bitLength = binaryDigits.length();
    byte[] packedBits = new byte[(bitLength + 7) / 8];
    for (int i = 0; i < bitLength; i++) {
      char c = binaryDigits.charAt(i);
      if (c == '1') {
        packedBits[i / 8] |= (byte) (0x80 >> (i % 8));
      } else if (c != '0') {
        throw new IllegalArgumentException("Invalid bit vector text: " + binaryDigits);
      }
    }
    return new BitVector(packedBits, bitLength);
  }

  private BitVector(byte[] packedBits, int bitLength) {
    this.packedBits = packedBits;
    this.bitLength = bitLength;
  }

  /**
   * @return the bits of this vector
   */
  public boolean[] toBooleanArray() {
    boolean[] bits = new boolean[bitLength];
    for (int i = 0; i < bitLength; i++) {
      bits[i] = (packedBits[i / 8] & (0x80 >> (i % 8))) != 0;
    }
    return bits;
  }

  /**
   * @return the bits of this vector packed most significant bit first; the internal array is
   * returned, mutations are visible
   */
  public byte[] toPackedByteArray() {
    return packedBits;
  }

  @Override
  public int dimension() {
    return bitLength;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o instanceof BitVector) {
      BitVector that = (BitVector) o;
      return bitLength == that.bitLength && Arrays.equals(packedBits, that.packedBits);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return 31 * bitLength + Arrays.hashCode(packedBits);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(bitLength);
    for (int i = 0; i < bitLength; i++) {
      sb.append((packedBits[i / 8] & (0x80 >> (i % 8))) != 0 ? '1' : '0');
    }
    return sb.toString();
  }
}

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

package io.vertx.tests.sqlclient.data;

import io.vertx.sqlclient.data.BitVector;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class BitVectorTest {

  @Test
  public void testTextRoundTrip() {
    assertEquals("10110100", BitVector.create("10110100").toString());
    assertEquals("", BitVector.create("").toString());
    assertEquals("0", BitVector.create("0").toString());
    assertEquals("1", BitVector.create("1").toString());
  }

  @Test
  public void testPacking() {
    BitVector vector = BitVector.create("10110100");
    assertEquals(8, vector.dimension());
    assertArrayEquals(new byte[]{(byte) 0xB4}, vector.toPackedByteArray());
    assertArrayEquals(new boolean[]{true, false, true, true, false, true, false, false}, vector.toBooleanArray());
    assertEquals("10110100", vector.toString());
  }

  @Test
  public void testOddBitLength() {
    // 10 bits: the last byte holds two significant bits, most significant bit first
    BitVector vector = BitVector.create("1011010010");
    assertEquals(10, vector.dimension());
    assertArrayEquals(new byte[]{(byte) 0xB4, (byte) 0x80}, vector.toPackedByteArray());
    assertEquals("1011010010", vector.toString());
    assertArrayEquals(
      new boolean[]{true, false, true, true, false, true, false, false, true, false},
      vector.toBooleanArray());
    // boolean round trip
    assertEquals(vector, BitVector.create(vector.toBooleanArray()));
  }

  @Test
  public void testCreateFromBooleans() {
    boolean[] bits = {true, false, true, true, false, true, false, false};
    BitVector vector = BitVector.create(bits);
    assertEquals(8, vector.dimension());
    assertArrayEquals(new byte[]{(byte) 0xB4}, vector.toPackedByteArray());
    assertArrayEquals(bits, vector.toBooleanArray());
    assertEquals("10110100", vector.toString());
  }

  @Test
  public void testCreateFromPackedBits() {
    byte[] packedBits = {(byte) 0xB4};
    BitVector vector = BitVector.create(packedBits, 8);
    // the internal array is returned
    assertSame(packedBits, vector.toPackedByteArray());
    assertEquals(8, vector.dimension());
    assertEquals("10110100", vector.toString());
    // odd bit length: only the significant bits are exposed
    BitVector odd = BitVector.create(new byte[]{(byte) 0xB4, (byte) 0x80}, 10);
    assertEquals(10, odd.dimension());
    assertEquals("1011010010", odd.toString());
  }

  @Test
  public void testEqualsHashCode() {
    BitVector fromText = BitVector.create("10110100");
    BitVector fromBooleans = BitVector.create(new boolean[]{true, false, true, true, false, true, false, false});
    BitVector fromPacked = BitVector.create(new byte[]{(byte) 0xB4}, 8);
    assertEquals(fromText, fromBooleans);
    assertEquals(fromText, fromPacked);
    assertEquals(fromText.hashCode(), fromPacked.hashCode());
    assertEquals(fromText, fromText);
    assertNotEquals(fromText, null);
    assertNotEquals(fromText, "10110100");
    assertNotEquals(fromText, BitVector.create("10110101"));
    assertNotEquals(fromText, BitVector.create("1011010"));
    // the trailing padding bits are part of the value
    assertNotEquals(BitVector.create("1011"), BitVector.create(new byte[]{(byte) 0xB4}, 4));
  }

  @Test
  public void testMalformedInput() {
    assertIllegalArgumentException(() -> BitVector.create("10a1"));
    assertIllegalArgumentException(() -> BitVector.create("1011010 "));
    assertIllegalArgumentException(() -> BitVector.create("[1,0]"));
    assertIllegalArgumentException(() -> BitVector.create(new byte[1], -1));
    assertIllegalArgumentException(() -> BitVector.create(new byte[2], 8));
    assertIllegalArgumentException(() -> BitVector.create(new byte[0], 1));
    try {
      BitVector.create((boolean[]) null);
      fail("Should throw NullPointerException");
    } catch (NullPointerException e) {
      // expected
    }
    try {
      BitVector.create((byte[]) null, 0);
      fail("Should throw NullPointerException");
    } catch (NullPointerException e) {
      // expected
    }
    try {
      BitVector.create((String) null);
      fail("Should throw NullPointerException");
    } catch (NullPointerException e) {
      // expected
    }
  }

  private static void assertIllegalArgumentException(Runnable action) {
    try {
      action.run();
      fail("Should throw IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }
}

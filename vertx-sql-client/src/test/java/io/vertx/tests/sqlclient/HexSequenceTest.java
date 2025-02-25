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

package io.vertx.tests.sqlclient;

import io.vertx.sqlclient.impl.HexSequence;
import org.junit.Assert;
import org.junit.Test;

public class HexSequenceTest {

  @Test
  public void testStartValue() {
    assertEquals("0000000",   new HexSequence().next());
    assertEquals("0000000",   new HexSequence(0).next());
    assertEquals("000000A",   new HexSequence(0xA).next());
    assertEquals("000FFFF",   new HexSequence(0xFFFF).next());
    assertEquals("00010000",  new HexSequence(0x10000).next());
    assertEquals("000ABCDEF", new HexSequence(0xABCDEF).next());
  }

  @Test
  public void testSequence() {
    HexSequence seq = new HexSequence();
    assertEquals("0000000", seq.next());
    assertEquals("0000001", seq.next());
    assertEquals("0000002", seq.next());
    assertEquals("0000003", seq.next());
    assertEquals("0000004", seq.next());
    assertEquals("0000005", seq.next());
    assertEquals("0000006", seq.next());
    assertEquals("0000007", seq.next());
    assertEquals("0000008", seq.next());
    assertEquals("0000009", seq.next());
    assertEquals("000000A", seq.next());
    assertEquals("000000B", seq.next());
    assertEquals("000000C", seq.next());
    assertEquals("000000D", seq.next());
    assertEquals("000000E", seq.next());
    assertEquals("000000F", seq.next());
    assertEquals("0000010", seq.next());
    assertEquals("0000011", seq.next());
  }

  @Test
  public void testIncrement() {
    assertIncrement("0000100",    0x00000FF);
    assertIncrement("0000F00",    0x0000EFF);
    assertIncrement("0001000",    0x0000FFF);
    assertIncrement("000A000",    0x0009FFF);
    assertIncrement("00010000",   0x000FFFF);
    assertIncrement("000100000",  0x000FFFFF);
    assertIncrement("0001000000", 0x000FFFFFF);
    assertIncrement("000FFFFFFFFFFFFFFFF", 0x000FFFFFFFFFFFFFFFEL);
    assertIncrement("0000000",             0x000FFFFFFFFFFFFFFFFL);
    assertIncrement("0000000",             -1);
  }

  private static void assertIncrement(String incremented, long start) {
    HexSequence hex = new HexSequence(start);
    hex.next();
    assertEquals(incremented, hex.next());
  }

  private static void assertEquals(String expected, byte[] actual) {
    Assert.assertEquals(expected + "\0", new String(actual));
  }
}

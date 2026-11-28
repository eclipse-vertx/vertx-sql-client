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

import io.vertx.sqlclient.data.DoubleVector;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class DoubleVectorTest {

  @Test
  public void testTextRoundTrip() {
    assertEquals("[0.1,0.2,0.3]", DoubleVector.create("[0.1,0.2,0.3]").toString());
    // spaces are accepted, the output is canonical
    assertEquals("[0.1,0.2,0.3]", DoubleVector.create("[ 0.1 , 0.2 , 0.3 ]").toString());
    assertEquals("[1.0,2.0,3.0]", DoubleVector.create("[1,2,3]").toString());
    assertEquals("[0.5,-0.25,42.0]", DoubleVector.create("[0.5,-0.25,42]").toString());
  }

  @Test
  public void testEmptyVector() {
    assertEquals(0, DoubleVector.create("[]").dimension());
    assertEquals("[]", DoubleVector.create("[]").toString());
    assertEquals(0, DoubleVector.create("[ ]").dimension());
    assertEquals(0, DoubleVector.create(new double[0]).dimension());
    assertEquals("[]", DoubleVector.create(new double[0]).toString());
  }

  @Test
  public void testArrayRoundTrip() {
    double[] values = {0.1, 0.2, 0.3};
    DoubleVector vector = DoubleVector.create(values);
    assertEquals(3, vector.dimension());
    // the internal array is returned
    assertSame(values, vector.toDoubleArray());
    assertEquals("[0.1,0.2,0.3]", vector.toString());
  }

  @Test
  public void testLazyParse() {
    // create does not parse the components: a malformed component is only rejected on access
    DoubleVector malformed = DoubleVector.create("[a,b]");
    try {
      malformed.toDoubleArray();
      fail("Should throw IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // expected
    }
    // the text is parsed once and the result is cached
    DoubleVector vector = DoubleVector.create("[0.1,0.2,0.3]");
    double[] first = vector.toDoubleArray();
    assertSame(first, vector.toDoubleArray());
    assertArrayEquals(new double[]{0.1, 0.2, 0.3}, first, 0.0);
  }

  @Test
  public void testCreateFromFloats() {
    // widening
    DoubleVector vector = DoubleVector.create(new float[]{0.1f, 0.2f, 0.3f});
    assertEquals(3, vector.dimension());
    assertArrayEquals(new double[]{0.1f, 0.2f, 0.3f}, vector.toDoubleArray(), 0.0);
    // lossy narrowing
    assertArrayEquals(new float[]{0.1f, 0.2f, 0.3f}, vector.toFloatArray(), 0.0f);
  }

  @Test
  public void testEqualsHashCode() {
    DoubleVector fromText = DoubleVector.create("[0.1,0.2,0.3]");
    DoubleVector fromArray = DoubleVector.create(new double[]{0.1, 0.2, 0.3});
    DoubleVector fromFloats = DoubleVector.create(new float[]{0.1f, 0.2f, 0.3f});
    assertEquals(fromText, fromArray);
    assertEquals(fromText.hashCode(), fromArray.hashCode());
    assertEquals(fromText, fromText);
    assertNotEquals(fromText, null);
    assertNotEquals(fromText, "[0.1,0.2,0.3]");
    // 0.1f widened to double is not equal to the double literal 0.1
    assertNotEquals(fromText, fromFloats);
    assertNotEquals(fromText, DoubleVector.create("[0.1,0.2]"));
    assertNotEquals(fromText, DoubleVector.create("[0.1,0.2,0.4]"));
    assertNotEquals(fromText, DoubleVector.create("[0.1,0.2,0.3,0.4]"));
  }

  @Test
  public void testMalformedInput() {
    assertIllegalArgumentException(() -> DoubleVector.create("0.1,0.2"));
    assertIllegalArgumentException(() -> DoubleVector.create("[0.1,0.2"));
    assertIllegalArgumentException(() -> DoubleVector.create("0.1,0.2]"));
    assertIllegalArgumentException(() -> DoubleVector.create(""));
    assertIllegalArgumentException(() -> DoubleVector.create("["));
    // component errors surface on access (lazy parsing)
    assertIllegalArgumentException(() -> DoubleVector.create("[a,b]").toDoubleArray());
    assertIllegalArgumentException(() -> DoubleVector.create("[0.1,bogus]").toDoubleArray());
    assertIllegalArgumentException(() -> DoubleVector.create("[,]").toDoubleArray());
    try {
      DoubleVector.create((String) null);
      fail("Should throw NullPointerException");
    } catch (NullPointerException e) {
      // expected
    }
    try {
      DoubleVector.create((double[]) null);
      fail("Should throw NullPointerException");
    } catch (NullPointerException e) {
      // expected
    }
    try {
      DoubleVector.create((float[]) null);
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

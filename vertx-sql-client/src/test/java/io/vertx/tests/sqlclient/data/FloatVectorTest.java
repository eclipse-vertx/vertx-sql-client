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

import io.vertx.sqlclient.data.FloatVector;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class FloatVectorTest {

  @Test
  public void testTextRoundTrip() {
    assertEquals("[0.1,0.2,0.3]", FloatVector.create("[0.1,0.2,0.3]").toString());
    // spaces are accepted, the output is canonical
    assertEquals("[0.1,0.2,0.3]", FloatVector.create("[ 0.1 , 0.2 , 0.3 ]").toString());
    assertEquals("[1.0,2.0,3.0]", FloatVector.create("[1,2,3]").toString());
    assertEquals("[0.5,-0.25,42.0]", FloatVector.create("[0.5,-0.25,42]").toString());
  }

  @Test
  public void testEmptyVector() {
    assertEquals(0, FloatVector.create("[]").dimension());
    assertEquals("[]", FloatVector.create("[]").toString());
    assertEquals(0, FloatVector.create("[ ]").dimension());
    assertEquals(0, FloatVector.create(new float[0]).dimension());
    assertEquals("[]", FloatVector.create(new float[0]).toString());
  }

  @Test
  public void testArrayRoundTrip() {
    float[] values = {0.1f, 0.2f, 0.3f};
    FloatVector vector = FloatVector.create(values);
    assertEquals(3, vector.dimension());
    // the internal array is returned
    assertSame(values, vector.toFloatArray());
    assertEquals("[0.1,0.2,0.3]", vector.toString());
  }

  @Test
  public void testLazyParse() {
    // create does not parse the components: a malformed component is only rejected on access
    FloatVector malformed = FloatVector.create("[a,b]");
    try {
      malformed.toFloatArray();
      fail("Should throw IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // expected
    }
    // the text is parsed once and the result is cached
    FloatVector vector = FloatVector.create("[0.1,0.2,0.3]");
    float[] first = vector.toFloatArray();
    assertSame(first, vector.toFloatArray());
    assertArrayEquals(new float[]{0.1f, 0.2f, 0.3f}, first, 0.0f);
  }

  @Test
  public void testEqualsHashCode() {
    FloatVector fromText = FloatVector.create("[0.1,0.2,0.3]");
    FloatVector fromArray = FloatVector.create(new float[]{0.1f, 0.2f, 0.3f});
    assertEquals(fromText, fromArray);
    assertEquals(fromText.hashCode(), fromArray.hashCode());
    assertEquals(fromText, fromText);
    assertNotEquals(fromText, null);
    assertNotEquals(fromText, "[0.1,0.2,0.3]");
    assertNotEquals(fromText, FloatVector.create("[0.1,0.2]"));
    assertNotEquals(fromText, FloatVector.create("[0.1,0.2,0.4]"));
    assertNotEquals(fromText, FloatVector.create("[0.1,0.2,0.3,0.4]"));
  }

  @Test
  public void testMalformedInput() {
    assertIllegalArgumentException(() -> FloatVector.create("0.1,0.2"));
    assertIllegalArgumentException(() -> FloatVector.create("[0.1,0.2"));
    assertIllegalArgumentException(() -> FloatVector.create("0.1,0.2]"));
    assertIllegalArgumentException(() -> FloatVector.create(""));
    assertIllegalArgumentException(() -> FloatVector.create("["));
    assertIllegalArgumentException(() -> FloatVector.create("null"));
    // component errors surface on access (lazy parsing)
    assertIllegalArgumentException(() -> FloatVector.create("[a,b]").toFloatArray());
    assertIllegalArgumentException(() -> FloatVector.create("[0.1,bogus]").toFloatArray());
    assertIllegalArgumentException(() -> FloatVector.create("[,]").toFloatArray());
    try {
      FloatVector.create((String) null);
      fail("Should throw NullPointerException");
    } catch (NullPointerException e) {
      // expected
    }
    try {
      FloatVector.create((float[]) null);
      fail("Should throw NullPointerException");
    } catch (NullPointerException e) {
      // expected
    }
  }

  @Test
  public void testCreateFromDoubles() {
    // narrowing
    FloatVector vector = FloatVector.create(new double[]{0.1, 0.2, 0.3});
    assertEquals(3, vector.dimension());
    assertArrayEquals(new float[]{0.1f, 0.2f, 0.3f}, vector.toFloatArray(), 0.0f);
    // widening copy
    double[] doubles = vector.toDoubleArray();
    assertArrayEquals(new double[]{0.1f, 0.2f, 0.3f}, doubles, 0.0);
    assertNotSame(doubles, vector.toDoubleArray());
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

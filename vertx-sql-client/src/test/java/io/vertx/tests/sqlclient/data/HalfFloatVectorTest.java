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

import io.vertx.sqlclient.data.HalfFloatVector;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class HalfFloatVectorTest {

  @Test
  public void testTextRoundTrip() {
    assertEquals("[0.1,0.2,0.3]", HalfFloatVector.create("[0.1,0.2,0.3]").toString());
    // spaces are accepted, the output is canonical
    assertEquals("[0.1,0.2,0.3]", HalfFloatVector.create("[ 0.1 , 0.2 , 0.3 ]").toString());
    assertEquals("[1.0,2.0,3.0]", HalfFloatVector.create("[1,2,3]").toString());
    assertEquals("[0.5,-0.25,42.0]", HalfFloatVector.create("[0.5,-0.25,42]").toString());
  }

  @Test
  public void testEmptyVector() {
    assertEquals(0, HalfFloatVector.create("[]").dimension());
    assertEquals("[]", HalfFloatVector.create("[]").toString());
    assertEquals(0, HalfFloatVector.create("[ ]").dimension());
    assertEquals(0, HalfFloatVector.create(new float[0]).dimension());
    assertEquals("[]", HalfFloatVector.create(new float[0]).toString());
  }

  @Test
  public void testArrayRoundTrip() {
    float[] values = {0.1f, 0.2f, 0.3f};
    HalfFloatVector vector = HalfFloatVector.create(values);
    assertEquals(3, vector.dimension());
    // the internal array is returned
    assertSame(values, vector.toFloatArray());
    assertEquals("[0.1,0.2,0.3]", vector.toString());
  }

  @Test
  public void testLazyParse() {
    // create does not parse the components: a malformed component is only rejected on access
    HalfFloatVector malformed = HalfFloatVector.create("[a,b]");
    try {
      malformed.toFloatArray();
      fail("Should throw IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // expected
    }
    // the text is parsed once and the result is cached
    HalfFloatVector vector = HalfFloatVector.create("[0.1,0.2,0.3]");
    float[] first = vector.toFloatArray();
    assertSame(first, vector.toFloatArray());
    assertArrayEquals(new float[]{0.1f, 0.2f, 0.3f}, first, 0.0f);
  }

  @Test
  public void testEqualsHashCode() {
    HalfFloatVector fromText = HalfFloatVector.create("[0.1,0.2,0.3]");
    HalfFloatVector fromArray = HalfFloatVector.create(new float[]{0.1f, 0.2f, 0.3f});
    assertEquals(fromText, fromArray);
    assertEquals(fromText.hashCode(), fromArray.hashCode());
    assertEquals(fromText, fromText);
    assertNotEquals(fromText, null);
    assertNotEquals(fromText, "[0.1,0.2,0.3]");
    assertNotEquals(fromText, HalfFloatVector.create("[0.1,0.2]"));
    assertNotEquals(fromText, HalfFloatVector.create("[0.1,0.2,0.4]"));
    assertNotEquals(fromText, HalfFloatVector.create("[0.1,0.2,0.3,0.4]"));
  }

  @Test
  public void testMalformedInput() {
    assertIllegalArgumentException(() -> HalfFloatVector.create("0.1,0.2"));
    assertIllegalArgumentException(() -> HalfFloatVector.create("[0.1,0.2"));
    assertIllegalArgumentException(() -> HalfFloatVector.create("0.1,0.2]"));
    assertIllegalArgumentException(() -> HalfFloatVector.create(""));
    assertIllegalArgumentException(() -> HalfFloatVector.create("["));
    // component errors surface on access (lazy parsing)
    assertIllegalArgumentException(() -> HalfFloatVector.create("[a,b]").toFloatArray());
    assertIllegalArgumentException(() -> HalfFloatVector.create("[0.1,bogus]").toFloatArray());
    assertIllegalArgumentException(() -> HalfFloatVector.create("[,]").toFloatArray());
    try {
      HalfFloatVector.create((String) null);
      fail("Should throw NullPointerException");
    } catch (NullPointerException e) {
      // expected
    }
    try {
      HalfFloatVector.create((float[]) null);
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

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

import io.vertx.sqlclient.data.ByteVector;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class ByteVectorTest {

  @Test
  public void testTextRoundTrip() {
    assertEquals("[1,2,3]", ByteVector.create("[1,2,3]").toString());
    // spaces are accepted, the output is canonical
    assertEquals("[1,2,3]", ByteVector.create("[ 1 , 2 , 3 ]").toString());
    assertEquals("[1,-2,127,-128]", ByteVector.create("[1,-2,127,-128]").toString());
  }

  @Test
  public void testEmptyVector() {
    assertEquals(0, ByteVector.create("[]").dimension());
    assertEquals("[]", ByteVector.create("[]").toString());
    assertEquals(0, ByteVector.create("[ ]").dimension());
    assertEquals(0, ByteVector.create(new byte[0]).dimension());
    assertEquals("[]", ByteVector.create(new byte[0]).toString());
  }

  @Test
  public void testArrayRoundTrip() {
    byte[] values = {1, 2, 3};
    ByteVector vector = ByteVector.create(values);
    assertEquals(3, vector.dimension());
    // the internal array is returned
    assertSame(values, vector.toByteArray());
    assertEquals("[1,2,3]", vector.toString());
  }

  @Test
  public void testLazyParse() {
    // create does not parse the components: a malformed component is only rejected on access
    ByteVector malformed = ByteVector.create("[a,b]");
    try {
      malformed.toByteArray();
      fail("Should throw IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // expected
    }
    // the text is parsed once and the result is cached
    ByteVector vector = ByteVector.create("[1,2,3]");
    byte[] first = vector.toByteArray();
    assertSame(first, vector.toByteArray());
    assertArrayEquals(new byte[]{1, 2, 3}, first);
  }

  @Test
  public void testEqualsHashCode() {
    ByteVector fromText = ByteVector.create("[1,2,3]");
    ByteVector fromArray = ByteVector.create(new byte[]{1, 2, 3});
    assertEquals(fromText, fromArray);
    assertEquals(fromText.hashCode(), fromArray.hashCode());
    assertEquals(fromText, fromText);
    assertNotEquals(fromText, null);
    assertNotEquals(fromText, "[1,2,3]");
    assertNotEquals(fromText, ByteVector.create("[1,2]"));
    assertNotEquals(fromText, ByteVector.create("[1,2,4]"));
    assertNotEquals(fromText, ByteVector.create("[1,2,3,4]"));
  }

  @Test
  public void testMalformedInput() {
    assertIllegalArgumentException(() -> ByteVector.create("1,2,3"));
    assertIllegalArgumentException(() -> ByteVector.create("[1,2,3"));
    assertIllegalArgumentException(() -> ByteVector.create("1,2,3]"));
    assertIllegalArgumentException(() -> ByteVector.create(""));
    assertIllegalArgumentException(() -> ByteVector.create("["));
    // component errors surface on access (lazy parsing)
    assertIllegalArgumentException(() -> ByteVector.create("[a,b]").toByteArray());
    assertIllegalArgumentException(() -> ByteVector.create("[1,bogus]").toByteArray());
    assertIllegalArgumentException(() -> ByteVector.create("[,]").toByteArray());
    // out of byte range
    assertIllegalArgumentException(() -> ByteVector.create("[128]").toByteArray());
    assertIllegalArgumentException(() -> ByteVector.create("[-129]").toByteArray());
    try {
      ByteVector.create((String) null);
      fail("Should throw NullPointerException");
    } catch (NullPointerException e) {
      // expected
    }
    try {
      ByteVector.create((byte[]) null);
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

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

import io.vertx.sqlclient.data.SparseFloatVector;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class SparseFloatVectorTest {

  @Test
  public void testAccessors() {
    int[] indices = {0, 99};
    float[] values = {0.5f, -0.25f};
    SparseFloatVector vector = SparseFloatVector.create(65535, indices, values);
    assertEquals(65535, vector.dimension());
    // the internal arrays are returned
    assertSame(indices, vector.indices());
    assertSame(values, vector.values());
  }

  @Test
  public void testToString() {
    // one-based indices
    SparseFloatVector vector = SparseFloatVector.create(65535, new int[]{0, 99}, new float[]{0.5f, -0.25f});
    assertEquals("{1:0.5,100:-0.25}/65535", vector.toString());
    // single entry
    assertEquals("{1:0.5}/3", SparseFloatVector.create(3, new int[]{0}, new float[]{0.5f}).toString());
    // no entries
    assertEquals("{}/10", SparseFloatVector.create(10, new int[0], new float[0]).toString());
    // zero-dimension vector
    assertEquals("{}/0", SparseFloatVector.create(0, new int[0], new float[0]).toString());
  }

  @Test
  public void testZeroValuesAreAllowed() {
    // zero-valued entries are not rejected client-side, databases may reject them
    SparseFloatVector vector = SparseFloatVector.create(3, new int[]{1}, new float[]{0.0f});
    assertEquals(3, vector.dimension());
    assertArrayEquals(new int[]{1}, vector.indices());
    assertArrayEquals(new float[]{0.0f}, vector.values(), 0.0f);
  }

  @Test
  public void testEqualsHashCode() {
    SparseFloatVector vector = SparseFloatVector.create(65535, new int[]{0, 99}, new float[]{0.5f, -0.25f});
    assertEquals(vector, SparseFloatVector.create(65535, new int[]{0, 99}, new float[]{0.5f, -0.25f}));
    assertEquals(vector.hashCode(), SparseFloatVector.create(65535, new int[]{0, 99}, new float[]{0.5f, -0.25f}).hashCode());
    assertEquals(vector, vector);
    assertNotEquals(vector, null);
    assertNotEquals(vector, "{1:0.5,100:-0.25}/65535");
    // different dimensions
    assertNotEquals(vector, SparseFloatVector.create(65534, new int[]{0, 99}, new float[]{0.5f, -0.25f}));
    // different indices
    assertNotEquals(vector, SparseFloatVector.create(65535, new int[]{0, 98}, new float[]{0.5f, -0.25f}));
    // different values
    assertNotEquals(vector, SparseFloatVector.create(65535, new int[]{0, 99}, new float[]{0.5f, -0.26f}));
    // different number of entries
    assertNotEquals(vector, SparseFloatVector.create(65535, new int[]{0}, new float[]{0.5f}));
  }

  @Test
  public void testValidation() {
    // negative dimensions
    assertIllegalArgumentException(() -> SparseFloatVector.create(-1, new int[0], new float[0]));
    // mismatching lengths
    assertIllegalArgumentException(() -> SparseFloatVector.create(10, new int[]{0}, new float[0]));
    assertIllegalArgumentException(() -> SparseFloatVector.create(10, new int[0], new float[]{0.5f}));
    // out of bounds indices
    assertIllegalArgumentException(() -> SparseFloatVector.create(10, new int[]{-1}, new float[]{0.5f}));
    assertIllegalArgumentException(() -> SparseFloatVector.create(10, new int[]{10}, new float[]{0.5f}));
    // not strictly ascending
    assertIllegalArgumentException(() -> SparseFloatVector.create(10, new int[]{1, 1}, new float[]{0.5f, 0.5f}));
    assertIllegalArgumentException(() -> SparseFloatVector.create(10, new int[]{2, 1}, new float[]{0.5f, 0.5f}));
    try {
      SparseFloatVector.create(10, null, new float[0]);
      fail("Should throw NullPointerException");
    } catch (NullPointerException e) {
      // expected
    }
    try {
      SparseFloatVector.create(10, new int[0], null);
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

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
 * A sparse vector of single precision {@code float} components, holding only the non-zero
 * positions.
 * <p>
 * Instances map sparse vector types, e.g PostgreSQL {@code sparsevec} columns. The text
 * representation, e.g {@code "{1:0.5,100:-0.25}/65535"}, uses one-based indices.
 * <p>
 * Instances have value semantics: {@link #equals(Object)} and {@link #hashCode()} compare the
 * dimension, the indices and the values.
 */
public final class SparseFloatVector implements Vector {

  private final int dimensions;
  private final int[] indices;
  private final float[] values;

  /**
   * Create a {@code SparseFloatVector}.
   * <p>
   * The given arrays are stored without copy, mutations are visible. Zero-valued entries are
   * not rejected, databases may reject them.
   *
   * @param dimensions the total number of dimensions, including zero-valued ones
   * @param indices the zero-based positions of the components, strictly ascending
   * @param values the values of the components at these positions
   * @return the vector
   * @throws NullPointerException when {@code indices} or {@code values} is {@code null}
   * @throws IllegalArgumentException when {@code dimensions} is negative, when the arrays have
   * different lengths, or when {@code indices} are not strictly ascending within
   * {@code [0, dimensions)}
   */
  public static SparseFloatVector create(int dimensions, int[] indices, float[] values) {
    Objects.requireNonNull(indices, "indices cannot be null");
    Objects.requireNonNull(values, "values cannot be null");
    if (dimensions < 0) {
      throw new IllegalArgumentException("dimensions must be non-negative: " + dimensions);
    }
    if (indices.length != values.length) {
      throw new IllegalArgumentException("indices and values must have the same length: "
        + indices.length + " != " + values.length);
    }
    int previous = -1;
    for (int index : indices) {
      if (index < 0 || index >= dimensions) {
        throw new IllegalArgumentException("index out of bounds [0, " + dimensions + "): " + index);
      }
      if (index <= previous) {
        throw new IllegalArgumentException("indices must be strictly ascending: " + index + " follows " + previous);
      }
      previous = index;
    }
    return new SparseFloatVector(dimensions, indices, values);
  }

  private SparseFloatVector(int dimensions, int[] indices, float[] values) {
    this.dimensions = dimensions;
    this.indices = indices;
    this.values = values;
  }

  @Override
  public int dimension() {
    return dimensions;
  }

  /**
   * @return the zero-based positions of the components, strictly ascending; the internal
   * array is returned, mutations are visible
   */
  public int[] indices() {
    return indices;
  }

  /**
   * @return the values of the components at the positions returned by {@link #indices()}; the
   * internal array is returned, mutations are visible
   */
  public float[] values() {
    return values;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o instanceof SparseFloatVector) {
      SparseFloatVector that = (SparseFloatVector) o;
      return dimensions == that.dimensions
        && Arrays.equals(indices, that.indices)
        && Arrays.equals(values, that.values);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = dimensions;
    result = 31 * result + Arrays.hashCode(indices);
    result = 31 * result + Arrays.hashCode(values);
    return result;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("{");
    for (int i = 0; i < indices.length; i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append(indices[i] + 1).append(':').append(values[i]);
    }
    return sb.append('}').append('/').append(dimensions).toString();
  }
}

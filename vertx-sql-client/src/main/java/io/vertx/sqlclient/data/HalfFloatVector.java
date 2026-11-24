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
 * A vector of half precision components.
 * <p>
 * Java has no half precision floating point type, therefore the components are held as single
 * precision {@code float} values. Databases storing half precision vectors (e.g PostgreSQL
 * {@code halfvec} columns or SQL Server {@code VECTOR(n, FLOAT16)} columns) convert the
 * components lossily between the two precisions.
 * <p>
 * Instances can be created from an array of values or from the text representation shared by
 * most databases, e.g {@code "[0.1,0.2,0.3]"}. When created from text, the components are
 * parsed lazily on first access and the result is cached.
 * <p>
 * Instances are not thread-safe when created from text, due to the unsynchronized lazy parsing
 * of the text representation.
 * <p>
 * Instances have value semantics: {@link #equals(Object)} and {@link #hashCode()} compare the
 * dimension and the components, regardless of the representation an instance was created from.
 */
public final class HalfFloatVector implements Vector {

  private String text;
  private float[] values;

  /**
   * Create a {@code HalfFloatVector} from its text representation, e.g {@code "[0.1,0.2,0.3]"}.
   * <p>
   * The text is stored and parsed lazily when the components are first accessed. The overall
   * string is trimmed and must be a bracketed list of comma-separated components. An empty
   * list denotes a zero-dimension vector.
   *
   * @param text the text representation
   * @return the vector
   * @throws NullPointerException when {@code text} is {@code null}
   * @throws IllegalArgumentException when {@code text} is not a bracketed list
   */
  public static HalfFloatVector create(String text) {
    Objects.requireNonNull(text, "text cannot be null");
    VectorText.checkText(text);
    return new HalfFloatVector(text);
  }

  /**
   * Create a {@code HalfFloatVector} from its components.
   *
   * @param values the components
   * @return the vector
   * @throws NullPointerException when {@code values} is {@code null}
   */
  public static HalfFloatVector create(float[] values) {
    Objects.requireNonNull(values, "values cannot be null");
    return new HalfFloatVector(values);
  }

  private HalfFloatVector(String text) {
    this.text = text;
  }

  private HalfFloatVector(float[] values) {
    this.values = values;
  }

  /**
   * @return the components of this vector, parsed lazily from the text representation when
   * this vector was created from text; the internal array is returned, mutations are visible
   * @throws IllegalArgumentException when the text representation is malformed
   */
  public float[] toFloatArray() {
    float[] v = values;
    if (v == null) {
      v = values = VectorText.parseFloats(text);
    }
    return v;
  }

  @Override
  public int dimension() {
    return toFloatArray().length;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o instanceof HalfFloatVector) {
      HalfFloatVector that = (HalfFloatVector) o;
      return Arrays.equals(toFloatArray(), that.toFloatArray());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(toFloatArray());
  }

  @Override
  public String toString() {
    return VectorText.toString(toFloatArray());
  }
}

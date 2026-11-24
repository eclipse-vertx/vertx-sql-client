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

/**
 * A vector of values, mapping the vector data types provided by some databases.
 * <p>
 * The following implementations are provided:
 * <ul>
 *   <li>{@link FloatVector}: vectors of single precision {@code float} components</li>
 *   <li>{@link HalfFloatVector}: vectors of half precision components, held as {@code float} in Java</li>
 *   <li>{@link DoubleVector}: vectors of double precision {@code double} components</li>
 *   <li>{@link ByteVector}: vectors of signed {@code byte} components</li>
 *   <li>{@link BitVector}: vectors of bits, packed most significant bit first</li>
 *   <li>{@link SparseFloatVector}: sparse vectors of single precision {@code float} components</li>
 * </ul>
 */
public interface Vector {

  /**
   * @return the total number of dimensions of this vector
   */
  int dimension();
}

/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.oracleclient.data;

import io.vertx.core.buffer.Buffer;

import java.util.Arrays;
import java.util.Objects;

/**
 * An object that holds bytes that can be written to an Oracle {@code BLOB} column.
 */
public final class Blob {

  public final byte[] bytes;

  private Blob(byte[] bytes) {
    this.bytes = bytes;
  }

  public static Blob wrap(byte[] bytes) {
    return new Blob(Objects.requireNonNull(bytes));
  }

  public static Blob copy(Buffer buffer) {
    return new Blob(Objects.requireNonNull(buffer).getBytes());
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Blob blob = (Blob) o;
    return Arrays.equals(bytes, blob.bytes);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(bytes);
  }

  @Override
  public String toString() {
    return "Blob{" +
      "bytes=" + Arrays.toString(bytes) +
      '}';
  }
}

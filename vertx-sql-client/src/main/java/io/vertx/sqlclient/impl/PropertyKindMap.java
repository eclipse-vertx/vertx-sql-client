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

package io.vertx.sqlclient.impl;

import io.vertx.sqlclient.PropertyKind;

import java.util.Arrays;

/**
 * A map-like storage for {@link PropertyKind} instances.
 * It is suitable for our clients which store very few instances (usually just one).
 */
public final class PropertyKindMap {

  private Object[] elements;
  private int count;

  public PropertyKindMap() {
    elements = new Object[2];
    count = 0;
  }

  public Object get(PropertyKind<?> property) {
    for (int i = 0; i < count; i++) {
      int idx = 2 * i;
      if (property.equals(elements[idx])) {
        return elements[idx + 1];
      }
    }
    return null;
  }

  public void put(PropertyKind<?> property, Object value) {
    for (int i = 0; i < count; i++) {
      int idx = 2 * i;
      if (property.equals(elements[idx])) {
        elements[idx + 1] = value;
        return;
      }
    }
    int idx = 2 * count;
    if (idx == elements.length) {
      elements = Arrays.copyOf(elements, 2 * (count + 1));
    }
    elements[idx] = property;
    elements[idx + 1] = value;
    count++;
  }

  // visible for testing
  int count() {
    return count;
  }
}

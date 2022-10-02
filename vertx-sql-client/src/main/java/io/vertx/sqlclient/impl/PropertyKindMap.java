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
import java.util.Objects;

/**
 * A map-like storage for {@link PropertyKind} instances.
 * It is suitable for our clients which store very few instances (usually just one).
 */
public final class PropertyKindMap {

  private static final Object[] EMPTY_ELEMENTS = {};

  private Object[] elements;

  public PropertyKindMap() {
    elements = EMPTY_ELEMENTS;
  }

  public Object get(PropertyKind<?> property) {
    for (int i = 0; i < elements.length; i += 2) {
      Object key = elements[i];
      if (key == property || key.equals(property)) {
        return elements[i + 1];
      }
    }
    return null;
  }

  public void put(PropertyKind<?> property, Object value) {
    Objects.requireNonNull(property, "property is null");
    Objects.requireNonNull(value, "value is null");
    for (int i = 0; i < elements.length; i += 2) {
      Object key = elements[i];
      if (key == property || key.equals(property)) {
        elements[i + 1] = value;
        return;
      }
    }
    if (elements == EMPTY_ELEMENTS) {
      elements = new Object[2];
    } else {
      elements = Arrays.copyOf(elements, elements.length + 2);
    }
    elements[elements.length - 2] = property;
    elements[elements.length - 1] = value;
  }

  // visible for testing
  int count() {
    return elements.length >> 1;
  }
}

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

package io.vertx.tests.sqlclient;

import io.vertx.sqlclient.PropertyKind;
import io.vertx.sqlclient.impl.PropertyKindMap;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PropertyKindMapTest {

  private static final PropertyKind<?> PROP1 = PropertyKind.create("prop1", String.class);
  private static final PropertyKind<?> PROP2 = PropertyKind.create("prop2", Integer.class);
  private static final PropertyKind<?> PROP3 = PropertyKind.create("prop3", UUID.class);

  private static final UUID UUID = java.util.UUID.randomUUID();

  PropertyKindMap map = new PropertyKindMap();

  @Test
  public void testEmptyMap() {
    assertNull(map.get(PROP1));
    assertNull(map.get(PROP2));
    assertNull(map.get(PROP3));
    assertEquals(0, map.count());
  }

  @Test
  public void testLookups() {
    map.put(PROP1, "foo");
    map.put(PROP2, 42);
    map.put(PROP3, UUID);

    assertEquals("foo", map.get(PROP1));
    assertEquals(42, map.get(PROP2));
    assertEquals(UUID, map.get(PROP3));
    assertEquals(3, map.count());
  }

  @Test
  public void testDuplicateKey() {
    map.put(PROP1, "foo");
    map.put(PROP2, 42);
    map.put(PROP3, UUID);
    map.put(PROP2, 43);

    assertEquals("foo", map.get(PROP1));
    assertEquals(43, map.get(PROP2));
    assertEquals(UUID, map.get(PROP3));
    assertEquals(3, map.count());
  }
}

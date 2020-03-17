/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
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
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class SqlResultTest {
  @Test
  public void testNullPropertyKind() {
    RowSetImpl rowSet = new RowSetImpl();
    try {
      rowSet.property(null);
    } catch (IllegalArgumentException e) {
      Assert.assertEquals("Property can not be null", e.getMessage());
    }
  }

  @Test
  public void testNullProperties() {
    RowSetImpl rowSet = new RowSetImpl();
    rowSet.properties = null;
    PropertyKind<String> propertyKind = () -> String.class;
    Assert.assertNull(rowSet.property(propertyKind));
  }

  @Test
  public void testPropertyKindNullType() {
    RowSetImpl rowSet = new RowSetImpl();
    rowSet.properties = new HashMap<>();
    PropertyKind<String> nullTypePropertyKind = () -> null;
    try {
      rowSet.property(nullTypePropertyKind);
    } catch (NullPointerException ignored) {
      // NPE
    }
  }

  @Test
  public void testUnknownPropertyKind() {
    RowSetImpl rowSet = new RowSetImpl();
    PropertyKind<Integer> knownPropertyKind = () -> Integer.class;
    PropertyKind<String> unknownPropertyKind = () -> String.class;
    rowSet.properties = new HashMap<>();
    rowSet.properties.put(knownPropertyKind, 1234);

    Assert.assertEquals(Integer.valueOf(1234), rowSet.property(knownPropertyKind));
    Assert.assertNull(rowSet.property(unknownPropertyKind));
  }
}

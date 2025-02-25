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

package io.vertx.tests.sqlclient;

import io.vertx.sqlclient.PropertyKind;
import io.vertx.sqlclient.impl.PropertyKindMap;
import io.vertx.sqlclient.impl.RowSetImpl;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.fail;

public class SqlResultTest {

  static class TestRowSet extends RowSetImpl<Object> {

    void properties(PropertyKindMap props) {
      this.properties = props;
    }
  }

  @Test
  public void testNullPropertyKind() {
    TestRowSet rowSet = new TestRowSet();
    try {
      rowSet.property(null);
    } catch (IllegalArgumentException e) {
      Assert.assertEquals("Property can not be null", e.getMessage());
    }
  }

  @Test
  public void testNullProperties() {
    TestRowSet rowSet = new TestRowSet();
    rowSet.properties(null);
    PropertyKind<String> propertyKind = PropertyKind.create("test", String.class);
    Assert.assertNull(rowSet.property(propertyKind));
  }

  @Test
  public void testPropertyKindNullType() {
    TestRowSet rowSet = new TestRowSet();
    rowSet.properties(new PropertyKindMap());
    PropertyKind<String> nullTypePropertyKind = new PropertyKind<String>() {
      @Override
      public String name() {
        return "test";
      }

      @Override
      public Class<String> type() {
        return null;
      }
    };
    try {
      rowSet.property(nullTypePropertyKind);
    } catch (NullPointerException ignored) {
      // NPE
    }
  }

  @Test
  public void testUnknownPropertyKind() {
    TestRowSet rowSet = new TestRowSet();
    PropertyKind<Integer> knownPropertyKind = PropertyKind.create("test-1", Integer.class);
    PropertyKind<String> unknownPropertyKind = PropertyKind.create("test-2", String.class);
    rowSet.properties(new PropertyKindMap().put(knownPropertyKind, 1234));

    Assert.assertEquals(Integer.valueOf(1234), rowSet.property(knownPropertyKind));
    Assert.assertNull(rowSet.property(unknownPropertyKind));
  }

  @Test
  public void testInvalidPropertyType() {
    TestRowSet rowSet = new TestRowSet();
    PropertyKind<Integer> propertyKind1 = PropertyKind.create("test", Integer.class);
    PropertyKind<String> propertyKind2 = PropertyKind.create("test", String.class);
    rowSet.properties(new PropertyKindMap().put(propertyKind1, 1234));

    Assert.assertEquals(Integer.valueOf(1234), rowSet.property(propertyKind1));
    try {
      rowSet.property(propertyKind2);
      fail();
    } catch (IllegalArgumentException ignore) {
    }
  }
}

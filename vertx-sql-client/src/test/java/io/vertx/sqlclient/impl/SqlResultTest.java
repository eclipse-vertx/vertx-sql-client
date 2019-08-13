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

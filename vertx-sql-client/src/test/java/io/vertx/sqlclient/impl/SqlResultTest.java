package io.vertx.sqlclient.impl;

import io.vertx.sqlclient.PropertyKind;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.fail;

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
    PropertyKind<String> propertyKind = PropertyKind.create("test", String.class);
    Assert.assertNull(rowSet.property(propertyKind));
  }

  @Test
  public void testPropertyKindNullType() {
    RowSetImpl rowSet = new RowSetImpl();
    rowSet.properties = new HashMap<>();
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
    RowSetImpl rowSet = new RowSetImpl();
    PropertyKind<Integer> knownPropertyKind = PropertyKind.create("test-1", Integer.class);
    PropertyKind<String> unknownPropertyKind = PropertyKind.create("test-2", String.class);
    rowSet.properties = new HashMap<>();
    rowSet.properties.put(knownPropertyKind, 1234);

    Assert.assertEquals(Integer.valueOf(1234), rowSet.property(knownPropertyKind));
    Assert.assertNull(rowSet.property(unknownPropertyKind));
  }

  @Test
  public void testInvalidPropertyType() {
    RowSetImpl rowSet = new RowSetImpl();
    PropertyKind<Integer> propertyKind1 = PropertyKind.create("test", Integer.class);
    PropertyKind<String> propertyKind2 = PropertyKind.create("test", String.class);
    rowSet.properties = new HashMap<>();
    rowSet.properties.put(propertyKind1, 1234);

    Assert.assertEquals(Integer.valueOf(1234), rowSet.property(propertyKind1));
    try {
      rowSet.property(propertyKind2);
      fail();
    } catch (IllegalArgumentException ignore) {
    }
  }
}

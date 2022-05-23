package io.vertx.pgclient.impl;

import io.vertx.pgclient.impl.codec.DataType;
import io.vertx.sqlclient.impl.RowDesc;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertNull;

public class RowImplTest {
  @Test
  public void testGetNullEnum() {
    RowImpl rowSet = new RowImpl(new RowDesc(Collections.singletonList("enum")));
    rowSet.addValue(null);
    assertNull(rowSet.get(DataType.class, 0));
  }
}

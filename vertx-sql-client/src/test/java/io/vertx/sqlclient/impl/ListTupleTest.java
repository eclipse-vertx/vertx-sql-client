package io.vertx.sqlclient.impl;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author: tk (rivers.boat.snow at gmail dot com)
 * @date: 2021/7/13
 */
public class ListTupleTest {
  @Test
  public void testFixedSizeList() {
    List<Object> fixedSizeList = Arrays.asList("tuple_value1", "tuple_value2");
    ListTuple listTuple = new ListTuple(fixedSizeList);
    listTuple.addValue("tuple_value3");
    assertEquals(3, listTuple.size());
    assertEquals("tuple_value3", listTuple.getValue(2));
  }

  @Test
  public void testReadOnlyList() {
    List<Object> readOnlyList = Collections.singletonList("tuple_value1");
    ListTuple listTuple = new ListTuple(readOnlyList);
    listTuple.addValue("tuple_value2");
    assertEquals(2, listTuple.size());
    assertEquals("tuple_value2", listTuple.getValue(1));
  }
}

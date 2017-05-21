package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.PostgresBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class BatchImpl implements PostgresBatch {

  final ArrayList<List<Object>> values = new ArrayList<>();

  @Override
  public PostgresBatch add(List<Object> params) {
    values.add(params);
    return this;
  }

  @Override
  public PostgresBatch add(Object param1) {
    return add(Collections.singletonList(param1));
  }

  @Override
  public PostgresBatch add(Object param1, Object param2) {
    return add(Arrays.asList(param1, param2));
  }

  @Override
  public PostgresBatch add(Object param1, Object param2, Object param3) {
    return add(Arrays.asList(param1, param2, param3));
  }

  @Override
  public PostgresBatch add(Object param1, Object param2, Object param3, Object param4) {
    return add(Arrays.asList(param1, param2, param3, param4));
  }

  @Override
  public PostgresBatch add(Object param1, Object param2, Object param3, Object param4, Object param5) {
    return add(Arrays.asList(param1, param2, param3, param4, param5));
  }

  @Override
  public PostgresBatch add(Object param1, Object param2, Object param3, Object param4, Object param5, Object param6) {
    return add(Arrays.asList(param1, param2, param3, param4, param5, param6));
  }
}

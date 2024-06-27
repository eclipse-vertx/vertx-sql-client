/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.sqlclient.templates.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.internal.TupleInternal;

import java.util.function.Function;

public class JsonTuple extends TupleInternal {

  private final int size;
  private final Function<Integer, String> columnMapping;
  private final JsonObject json;

  public JsonTuple(Function<Integer, String> columnMapping, int size, JsonObject json) {
    this.columnMapping = columnMapping;
    this.size = size;
    this.json = json;
  }

  @Override
  public Object getValueInternal(int pos) {
    String name = columnMapping.apply(pos);
    return json.getValue(name);
  }

  @Override
  public void setValue(int pos, Object value) {
    String name = columnMapping.apply(pos);
    json.put(name, value);
  }

  @Override
  public Tuple addValue(Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }
}

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

package io.vertx.sqlclient.impl;

import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.data.NullValue;

import java.util.ArrayList;
import java.util.List;

public interface TupleInternal extends Tuple {

  void setValue(int pos, Object value);

  @Override
  default Object getValue(int pos) {
    Object val = getValueInternal(pos);
    return val instanceof NullValue ? null : val;
  }

  Object getValueInternal(int pos);

  @Override
  default List<Class<?>> types() {
    int len = size();
    List<Class<?>> types = new ArrayList<>(len);
    for (int i = 0; i < len; i++) {
      Object param = getValueInternal(i);
      if (param instanceof NullValue) {
        types.add(((NullValue) param).type());
      } else if (param == null) {
        types.add(Object.class);
      } else {
        types.add(param.getClass());
      }
    }
    return types;
  }
}

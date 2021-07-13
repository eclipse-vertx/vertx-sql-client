/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.vertx.sqlclient.impl;

import io.vertx.sqlclient.Tuple;

import java.util.ArrayList;
import java.util.List;

public class ListTuple implements TupleInternal {

  private final List<Object> list = new ArrayList<>();

  /**
   * <p>if we get a fixed-size or read-only list(like {@link java.util.Arrays.ArrayList} or {@link java.util.Collections.SingletonList})
   * which have not override {@link java.util.AbstractList#add(Object)}
   * or {@link java.util.AbstractList#set(int, Object)} that defined in {@link java.util.AbstractList},
   * we can create a {@link ArrayList} to involve the {@param list}
   * to make the {@link #setValue(int, Object)} or {@link #addValue(Object)} work correctly
   * <p/>
   */
  public ListTuple(List<Object> list) {
    this.list.addAll(list);
  }

  @Override
  public Object getValueInternal(int pos) {
    return list.get(pos);
  }

  @Override
  public Tuple addValue(Object value) {
    list.add(value);
    return this;
  }

  @Override
  public void setValue(int pos, Object value) {
    list.set(pos, value);
  }

  @Override
  public int size() {
    return list.size();
  }

  @Override
  public void clear() {
    list.clear();
  }

}

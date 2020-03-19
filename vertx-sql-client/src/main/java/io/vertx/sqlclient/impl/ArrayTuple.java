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

import java.util.Collection;

public class ArrayTuple implements TupleInternal {

  public static Tuple EMPTY = new ArrayTuple(0);

  private Object[] values;
  private int size;

  public ArrayTuple(int len) {
    values = new Object[len];
    size = 0;
  }

  public ArrayTuple(Collection<?> c) {
    values = new Object[c.size()];
    size = 0;
    for (Object elt : c) {
      addValue(elt);
    }
  }

  public ArrayTuple(Tuple tuple) {
    values = new Object[tuple.size()];
    size = values.length;
    for (int idx = 0;idx < size;idx++) {
      values[idx] = tuple.getValue(idx);
    }
  }

  @Override
  public Object getValue(int pos) {
    return pos >= 0 && pos < size ? values[pos] : null;
  }

  @Override
  public Tuple addValue(Object value) {
    if (size >= values.length) {
      Object[] copy = new Object[values.length << 1 + 1];
      System.arraycopy(values, 0, copy, 0, values.length);
      values = copy;
    }
    values[size++] = value;
    return this;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public void clear() {
    for (int i = 0;i < values.length;i++) {
      values[i] = null;
    }
    size = 0;
  }

  @Override
  public void setValue(int pos, Object value) {
    if (pos < 0) {
      throw new IndexOutOfBoundsException("Invali position " + pos + ": must be > 0");
    }
    if (pos > size) {
      throw new IndexOutOfBoundsException("Invali position " + pos + ": must be < " + size);
    }
    values[pos] = value;
  }
  
  @Override
  public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append("[");
	for (int i = 0; i < size; i++) {
	  sb.append(values[i]);
	  if (i + 1 < size)
	    sb.append(",");
	}
	sb.append("]");
	return sb.toString();
  }
}

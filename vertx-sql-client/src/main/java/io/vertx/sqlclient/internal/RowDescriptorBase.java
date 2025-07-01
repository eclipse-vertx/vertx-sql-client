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

package io.vertx.sqlclient.internal;

import io.vertx.sqlclient.desc.ColumnDescriptor;
import io.vertx.sqlclient.desc.RowDescriptor;

import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
public class RowDescriptorBase implements RowDescriptor {

  private final ColumnNames columnNames;
  private final ColumnDescriptors columnDescriptors;

  public RowDescriptorBase(ColumnDescriptor[] columnDescriptors) {
    this.columnNames = new ColumnNames(columnDescriptors);
    this.columnDescriptors = new ColumnDescriptors(columnDescriptors);
  }

  public int columnIndex(String columnName) {
    if (columnName == null) {
      throw new NullPointerException("Column name must not be null");
    }
    return columnNames.indexOf(columnName);
  }

  public List<String> columnNames() {
    return columnNames;
  }

  public List<ColumnDescriptor> columnDescriptors() {
    return columnDescriptors;
  }

  @Override
  public String toString() {
    return "RowDesc{" +
      "columns=" + columnNames +
      '}';
  }

  private static class ColumnNames extends AbstractList<String> implements RandomAccess {

    private final ColumnDescriptor[] elements;

    ColumnNames(ColumnDescriptor[] elements) {
      this.elements = elements;
    }

    @Override
    public String get(int index) {
      return elements[index].name();
    }

    @Override
    public int size() {
      return elements.length;
    }

    @Override
    public int indexOf(Object o) {
      if (o != null) {
        for (int i = 0; i < elements.length; i++) {
          String name = elements[i].name();
          if (o.hashCode() == name.hashCode() && o.equals(name)) {
            return i;
          }
        }
      }
      return -1;
    }
  }

  private static class ColumnDescriptors extends AbstractList<ColumnDescriptor> implements RandomAccess {

    private final ColumnDescriptor[] elements;

    ColumnDescriptors(ColumnDescriptor[] elements) {
      this.elements = elements;
    }

    @Override
    public ColumnDescriptor get(int index) {
      return elements[index];
    }

    @Override
    public int size() {
      return elements.length;
    }
  }
}

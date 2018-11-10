package io.reactiverse.pgclient.copy;

import io.reactiverse.pgclient.codec.DataType;

public interface CopyData {

  static CopyData create(Object value) {
    return new CopyDataImpl(value);
  }

  static CopyData create(Object value, DataType type) {
    return new CopyDataImpl(value, type);
  }

  Object getValue();
  DataType getDataType();
}

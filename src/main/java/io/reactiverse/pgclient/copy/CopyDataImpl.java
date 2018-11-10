package io.reactiverse.pgclient.copy;

import io.reactiverse.pgclient.codec.DataType;

class CopyDataImpl implements CopyData {

  static DataType guessDataType(Object obj) {
    for (DataType dataType : DataType.values()) {
      if (dataType.encodingType != Object.class && dataType.encodingType.isAssignableFrom(obj.getClass())) {
        return dataType;
      }
    }
    return DataType.UNKNOWN;
  }

  private final Object value;
  private final DataType type;

  CopyDataImpl(Object value) {
    this(value, null);
  }

  CopyDataImpl(Object value, DataType type) {
    this.value = value;
    this.type = type;
  }

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public DataType getDataType() {
    if (type == null) {
      return guessDataType(value);
    }
    return type;
  }
}

package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;

import java.util.Map;

public class Enum8ColumnReader extends UInt8ColumnReader {
  public static final int ELEMENT_SIZE = 1;
  private final Map<Byte, String> enumVals;
  private final boolean enumsByName;

  public Enum8ColumnReader(int nRows, ClickhouseNativeColumnDescriptor descr, Map<? extends Number, String> enumVals, boolean enumsByName) {
    super(nRows, descr);
    this.enumVals = (Map<Byte, String>) enumVals;
    this.enumsByName = enumsByName;
  }

  @Override
  protected Object getElementInternal(int rowIdx, Class desired) {
    Byte key = (Byte) super.getElementInternal(rowIdx, desired);
    return recodeElement(key, desired);
  }

  private Object recodeElement(Byte key, Class desired) {
    if (Number.class.isAssignableFrom(desired)) {
      return key;
    }
    String str = enumVals.get(key);
    if (desired.isEnum()) {
      if (enumsByName) {
        return Enum.valueOf(desired, str);
      } else {
        return desired.getEnumConstants()[key];
      }
    }
    return str;
  }

  Object[] recodeValues(Object[] src, Class desired) {
    Byte[] bytes = (Byte[])src;
    if (desired == Object.class) {
      desired = String.class;
    }
    Object[] ret = (Object[]) java.lang.reflect.Array.newInstance(desired, src.length);
    for (int idx = 0; idx < ret.length; ++idx) {
      Byte el = bytes[idx];
      if (el != null) {
        ret[idx] = recodeElement(el, desired);
      }
    }
    return ret;
  }
}

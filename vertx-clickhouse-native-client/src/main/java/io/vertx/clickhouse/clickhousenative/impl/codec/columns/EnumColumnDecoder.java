package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import java.util.Map;

class EnumColumnDecoder {
  private final Map<? extends Number, String> enumKeyToName;
  private final boolean enumsByName;

  EnumColumnDecoder(Map<? extends Number, String> enumVals, boolean enumsByName) {
    this.enumKeyToName = enumVals;
    this.enumsByName = enumsByName;
  }

  public Object recodeElement(Number key, Class desired) {
    if (Number.class.isAssignableFrom(desired)) {
      return key;
    }
    String str = enumKeyToName.get(key);
    if (desired.isEnum()) {
      if (enumsByName) {
        return Enum.valueOf(desired, str);
      } else {
        return desired.getEnumConstants()[key.intValue()];
      }
    }
    return str;
  }

  Object[] recodeValues(Object[] src, Class desired) {
    Number[] bytes = (Number[])src;
    if (desired == Object.class) {
      desired = String.class;
    }
    Object[] ret = (Object[]) java.lang.reflect.Array.newInstance(desired, src.length);
    for (int idx = 0; idx < ret.length; ++idx) {
      Number el = bytes[idx];
      if (el != null) {
        ret[idx] = recodeElement(el, desired);
      }
    }
    return ret;
  }
}

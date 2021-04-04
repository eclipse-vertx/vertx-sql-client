package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import java.util.HashMap;
import java.util.Map;

public class EnumColumnEncoder {
  private final Map<? extends Number, String> enumKeyToName;
  private final Map<String, ? extends Number> enumNameToKey;
  private final boolean enumsByName;

  public EnumColumnEncoder(Map<? extends Number, String> enumKeyToName, boolean enumsByName) {
    this.enumKeyToName = enumKeyToName;
    this.enumNameToKey = buildReverseIndex(enumKeyToName);
    this.enumsByName = enumsByName;
  }

  private <R extends Number> Map<String, R> buildReverseIndex(Map<R, String> enumVals) {
    Map<String, R> ret = new HashMap<>();
    for (Map.Entry<R, String> entry : enumVals.entrySet()) {
      ret.put(entry.getValue(), entry.getKey());
    }
    return ret;
  }

  public Number encode(Object val) {
    Number idx;
    if (val.getClass() == String.class) {
      idx = enumNameToKey.get(val);
    } else if (val.getClass().isEnum()) {
      Enum enumVal = (Enum) val;
      if (enumsByName) {
        idx = enumNameToKey.get(enumVal.name());
      } else {
        Byte tmp = (byte) enumVal.ordinal();
        if (enumKeyToName.containsKey(tmp)) {
          idx = tmp;
        } else {
          idx = null;
        }
      }
    } else if (val instanceof Number) {
      idx = (Number) val;
    } else {
      throw new IllegalArgumentException("don't know how to serialize " + val + " of class " + val.getClass());
    }
    if (idx == null) {
      throw new IllegalArgumentException(val + " is not in dictionary; possible values: " + enumNameToKey.keySet());
    }
    return idx;
  }
}

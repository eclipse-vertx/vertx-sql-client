package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSink;
import io.vertx.sqlclient.Tuple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Enum8ColumnWriter extends UInt8ColumnWriter {
  private final Map<? extends Number, String> enumIndexToName;
  private final Map<String, ? extends Number> enumNameToIndex;
  private final boolean enumsByName;

  public Enum8ColumnWriter(List<Tuple> data, ClickhouseNativeColumnDescriptor columnDescriptor, Map<? extends Number, String> enumVals,
                           int columnIndex, boolean enumsByName) {
    super(data, columnDescriptor, columnIndex);
    this.enumIndexToName = enumVals;
    this.enumNameToIndex = buildReverseIndex(enumVals);
    this.enumsByName = enumsByName;
  }

  private <R extends Number> Map<String, R> buildReverseIndex(Map<R, String> enumVals) {
    Map<String, R> ret = new HashMap<>();
    for (Map.Entry<R, String> entry : enumVals.entrySet()) {
      ret.put(entry.getValue(), entry.getKey());
    }
    return ret;
  }

  @Override
  protected void serializeDataElement(ClickhouseStreamDataSink sink, Object val) {
    Number idx;
    if (val.getClass() == String.class) {
      idx = enumNameToIndex.get(val);
    } else if (val.getClass().isEnum()) {
      Enum enumVal = (Enum) val;
      if (enumsByName) {
        idx = enumNameToIndex.get(enumVal.name());
      } else {
        Byte tmp = (byte) enumVal.ordinal();
        if (enumIndexToName.containsKey(tmp)) {
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
      throw new IllegalArgumentException(val + " is not in dictionary; possible values: " + enumNameToIndex.keySet());
    }
    super.serializeDataElement(sink, idx);
  }
}

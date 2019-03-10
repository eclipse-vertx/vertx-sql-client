package io.reactiverse.myclient.impl.codec;

import io.reactiverse.sqlclient.impl.ParamDesc;
import io.reactiverse.myclient.impl.protocol.backend.ColumnDefinition;
import io.reactiverse.pgclient.impl.util.Util;

import java.util.List;
import java.util.stream.Stream;

public class MyParamDesc extends ParamDesc {
  private final ColumnDefinition[] paramDefinitions;

  public MyParamDesc(ColumnDefinition[] paramDefinitions) {
    this.paramDefinitions = paramDefinitions;
  }

  public ColumnDefinition[] paramDefinitions() {
    return paramDefinitions;
  }

  @Override
  public String prepare(List<Object> values) {
    if (values.size() != paramDefinitions.length) {
      return buildReport(values);
    }
//    for (int i = 0;i < paramDefinitions.length;i++) {
//      DataType paramDataType = paramDefinitions[i].getType();
//      Object value = values.get(i);
//      Object val = DataTypeCodec.prepare(paramDataType, value);
//      if (val != value) {
//        if (val == DataTypeCodec.REFUSED_SENTINEL) {
//          return buildReport(values);
//        } else {
//          values.set(i, val);
//        }
//      }
//    }
    // TODO we can't really achieve type check for params because MySQL prepare response does not provide any useful information for param definitions
    return null;
  }

  // reuse from pg
  private String buildReport(List<Object> values) {
    return Util.buildInvalidArgsError(values.stream(), Stream.of(paramDefinitions).map(paramDefinition -> paramDefinition.getType()).map(dataType -> dataType.decodingType));
  }
}

package io.vertx.mysqlclient.impl.codec;

import io.vertx.mysqlclient.impl.util.Util;
import io.vertx.sqlclient.impl.ParamDesc;

import java.util.List;
import java.util.stream.Stream;

class MySQLParamDesc extends ParamDesc {
  private final ColumnDefinition[] paramDefinitions;

  MySQLParamDesc(ColumnDefinition[] paramDefinitions) {
    this.paramDefinitions = paramDefinitions;
  }

  ColumnDefinition[] paramDefinitions() {
    return paramDefinitions;
  }

  @Override
  public String prepare(List<Object> values) {
    if (values.size() != paramDefinitions.length) {
      return buildReport(values);
    }
//    for (int i = 0;i < paramDefinitions.length;i++) {
//      DataType paramDataType = paramDefinitions[i].type();
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
    return Util.buildInvalidArgsError(values.stream(), Stream.of(paramDefinitions).map(paramDefinition -> paramDefinition.type()).map(dataType -> dataType.binaryType));
  }
}

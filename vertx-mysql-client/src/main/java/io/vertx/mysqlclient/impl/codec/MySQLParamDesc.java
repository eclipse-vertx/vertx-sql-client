package io.vertx.mysqlclient.impl.codec;

import io.vertx.sqlclient.impl.ErrorMessageFactory;
import io.vertx.sqlclient.impl.ParamDesc;
import io.vertx.sqlclient.impl.TupleInternal;

class MySQLParamDesc extends ParamDesc {
  private final ColumnDefinition[] paramDefinitions;

  MySQLParamDesc(ColumnDefinition[] paramDefinitions) {
    this.paramDefinitions = paramDefinitions;
  }

  ColumnDefinition[] paramDefinitions() {
    return paramDefinitions;
  }

  @Override
  public String prepare(TupleInternal values) {
    int numberOfParameters = values.size();
    int paramDescLength = paramDefinitions.length;
    if (numberOfParameters != paramDescLength) {
      return ErrorMessageFactory.buildWhenArgumentsLengthNotMatched(paramDescLength, numberOfParameters);
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
}

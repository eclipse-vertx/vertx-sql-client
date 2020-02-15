package io.vertx.mssqlclient.impl.codec;

import io.vertx.sqlclient.impl.ErrorMessageFactory;
import io.vertx.sqlclient.impl.ParamDesc;
import io.vertx.sqlclient.impl.TupleInternal;

class MSSQLParamDesc extends ParamDesc {
  private final ColumnData[] paramDescriptions;

  public MSSQLParamDesc(ColumnData[] paramDescriptions) {
    this.paramDescriptions = paramDescriptions;
  }

  public ColumnData[] paramDescriptions() {
    return paramDescriptions;
  }

  public String prepare(TupleInternal values) {
    int numberOfParameters = values.size();
    int paramDescLength = paramDescriptions.length;
    if (numberOfParameters != paramDescLength){
      return ErrorMessageFactory.buildWhenArgumentsLengthNotMatched(paramDescLength, numberOfParameters);
    }
    return null;
  }
}

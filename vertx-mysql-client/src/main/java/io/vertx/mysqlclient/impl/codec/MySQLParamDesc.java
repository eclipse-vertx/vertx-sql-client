package io.vertx.mysqlclient.impl.codec;

import io.vertx.sqlclient.impl.ErrorMessageFactory;
import io.vertx.sqlclient.impl.ParamDesc;
import io.vertx.sqlclient.impl.TupleInternal;

class MySQLParamDesc extends ParamDesc {
  private final ColumnDefinition[] paramDefinitions;
  private boolean sendTypesToServer;

  MySQLParamDesc(ColumnDefinition[] paramDefinitions) {
    this.paramDefinitions = paramDefinitions;
    this.sendTypesToServer = false;
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

    // binding the parameters
    boolean reboundParameters = false;
    for (int i = 0; i < values.size(); i++) {
      Object value = values.getValue(i);
      DataType dataType = DataTypeCodec.inferDataTypeByEncodingValue(value);
      DataType paramDataType = paramDefinitions[i].getType();
      if (paramDataType != dataType) {
        paramDefinitions[i].setType(dataType);
        reboundParameters = true;
      }
    }
    sendTypesToServer = reboundParameters; // parameter must be re-bound
    return null;
  }

  public boolean sendTypesToServer() {
    return sendTypesToServer;
  }
}

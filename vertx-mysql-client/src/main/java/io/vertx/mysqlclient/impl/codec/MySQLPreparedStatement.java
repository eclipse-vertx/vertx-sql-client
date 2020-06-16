/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.vertx.mysqlclient.impl.codec;

import io.vertx.mysqlclient.impl.MySQLParamDesc;
import io.vertx.mysqlclient.impl.MySQLRowDesc;
import io.vertx.mysqlclient.impl.datatype.DataType;
import io.vertx.mysqlclient.impl.datatype.DataTypeCodec;
import io.vertx.sqlclient.impl.*;

import java.util.Arrays;

class MySQLPreparedStatement implements PreparedStatement {

  final long statementId;
  final String sql;
  final MySQLParamDesc paramDesc;
  final MySQLRowDesc rowDesc;
  final boolean closeAfterUsage;

  private boolean sendTypesToServer;
  private final DataType[] bindingTypes;

  boolean isCursorOpen;

  MySQLPreparedStatement(String sql, long statementId, MySQLParamDesc paramDesc, MySQLRowDesc rowDesc, boolean closeAfterUsage) {
    this.statementId = statementId;
    this.paramDesc = paramDesc;
    this.rowDesc = rowDesc;
    this.sql = sql;
    this.closeAfterUsage = closeAfterUsage;

    this.bindingTypes = new DataType[paramDesc.paramDefinitions().length];
    // init param bindings
    cleanBindings();
  }

  @Override
  public ParamDesc paramDesc() {
    return paramDesc;
  }

  @Override
  public RowDesc rowDesc() {
    return rowDesc;
  }

  @Override
  public String sql() {
    return sql;
  }

  @Override
  public String prepare(TupleInternal values) {
    return bindParameters(paramDesc, values);
  }

  boolean sendTypesToServer() {
    return sendTypesToServer;
  }

  DataType[] bindingTypes() {
    return bindingTypes;
  }

  void cleanBindings() {
    this.sendTypesToServer = true;
    Arrays.fill(bindingTypes, DataType.UNBIND);
  }

  private String bindParameters(MySQLParamDesc paramDesc, TupleInternal params) {
    int numberOfParameters = params.size();
    int paramDescLength = paramDesc.paramDefinitions().length;
    if (numberOfParameters != paramDescLength) {
      return ErrorMessageFactory.buildWhenArgumentsLengthNotMatched(paramDescLength, numberOfParameters);
    }

    // binding the parameters
    boolean reboundParameters = false;
    for (int i = 0; i < params.size(); i++) {
      Object value = params.getValue(i);
      DataType dataType = DataTypeCodec.inferDataTypeByEncodingValue(value);
      DataType paramDataType = bindingTypes[i];
      if (paramDataType != dataType) {
        bindingTypes[i] = dataType;
        reboundParameters = true;
      }
    }
    sendTypesToServer = reboundParameters; // parameter must be re-bound
    return null;
  }
}

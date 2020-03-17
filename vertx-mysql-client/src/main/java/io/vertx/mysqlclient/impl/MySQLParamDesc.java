/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mysqlclient.impl;

import io.vertx.mysqlclient.impl.datatype.DataType;
import io.vertx.mysqlclient.impl.datatype.DataTypeCodec;
import io.vertx.mysqlclient.impl.protocol.ColumnDefinition;
import io.vertx.sqlclient.impl.ErrorMessageFactory;
import io.vertx.sqlclient.impl.ParamDesc;
import io.vertx.sqlclient.impl.TupleInternal;

public class MySQLParamDesc extends ParamDesc {
  private final ColumnDefinition[] paramDefinitions;
  private boolean sendTypesToServer;

  public MySQLParamDesc(ColumnDefinition[] paramDefinitions) {
    this.paramDefinitions = paramDefinitions;
    this.sendTypesToServer = false;
  }

  public ColumnDefinition[] paramDefinitions() {
    return paramDefinitions;
  }

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

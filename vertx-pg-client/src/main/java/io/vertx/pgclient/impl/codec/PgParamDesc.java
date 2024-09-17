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
package io.vertx.pgclient.impl.codec;

import io.vertx.sqlclient.impl.ErrorMessageFactory;
import io.vertx.sqlclient.internal.ParamDesc;
import io.vertx.sqlclient.internal.TupleInternal;

import java.util.Arrays;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
class PgParamDesc extends ParamDesc {

  // OIDs
  private final DataType[] paramDataTypes;

  PgParamDesc(DataType[] paramDataTypes) {
    this.paramDataTypes = paramDataTypes;
  }

  DataType[] paramDataTypes() {
    return paramDataTypes;
  }

  public String prepare(TupleInternal values) {
    int numberOfParams = values.size();
    if (numberOfParams > 65535) {
      return "The number of parameters (" + numberOfParams + ") exceeds the maximum of 65535. Use arrays or split the query.";
    }
    int paramDescLength = paramDataTypes.length;
    if (numberOfParams != paramDescLength) {
      return ErrorMessageFactory.buildWhenArgumentsLengthNotMatched(paramDescLength, numberOfParams);
    }
    for (int i = 0; i < paramDescLength; i++) {
      DataType paramDataType = paramDataTypes[i];
      ParamExtractor<?> extractor = paramDataType.paramExtractor;
      Object val;
      try {
        val = extractor.get(values, i);
      } catch (Exception e) {
        return ErrorMessageFactory.buildWhenArgumentsTypeNotMatched(paramDataType.decodingType, i, values.getValue(i));
      }
      values.setValue(i, val);
    }
    return null;
  }

  @Override
  public String toString() {
    return "PgParamDesc{" +
      "paramDataTypes=" + Arrays.toString(paramDataTypes) +
      '}';
  }
}

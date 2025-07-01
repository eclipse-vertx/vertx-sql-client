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

import io.vertx.core.VertxException;
import io.vertx.sqlclient.impl.ErrorMessageFactory;
import io.vertx.sqlclient.internal.ArrayTuple;
import io.vertx.sqlclient.internal.TupleBase;

import java.util.Arrays;
import java.util.function.Function;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
class PgParamDesc {

  // OIDs
  private final DataType[] paramDataTypes;

  PgParamDesc(DataType[] paramDataTypes) {
    this.paramDataTypes = paramDataTypes;
  }

  DataType[] paramDataTypes() {
    return paramDataTypes;
  }

  public TupleBase prepare(TupleBase values) {
    int numberOfParams = values.size();
    if (numberOfParams > 65535) {
      throw new VertxException("The number of parameters (" + numberOfParams + ") exceeds the maximum of 65535. Use arrays or split the query.", true);
    }
    int paramDescLength = paramDataTypes.length;
    if (numberOfParams != paramDescLength) {
      throw new VertxException(ErrorMessageFactory.buildWhenArgumentsLengthNotMatched(paramDescLength, numberOfParams), true);
    }
    TupleBase prepared = values;
    for (int i = 0; i < paramDescLength; i++) {
      DataType paramDataType = paramDataTypes[i];
      ParamExtractor<?> extractor = paramDataType.paramExtractor;
      Object val;
      try {
        val = extractor.get(values, i);
      } catch (Exception e) {
        throw new VertxException(ErrorMessageFactory.buildWhenArgumentsTypeNotMatched(paramDataType.decodingType, i, values.getValue(i)), true);
      }
      if (val != null) {
        Function<Object, Object> preparator = paramDataType.preEncoder;
        if (preparator != null) {
          if (prepared == values) {
            prepared = new ArrayTuple(prepared);
          }
          if (paramDataType.array) {
            Object[] array = (Object[]) val;
            Object[] tmp = new Object[array.length];
            for (int j = 0;j < array.length;j++) {
              tmp[j] = preparator.apply(array[j]);
            }
            val = tmp;
          } else {
            val = preparator.apply(val);
          }
        }
      }
      prepared.setValue(i, val);
    }
    return prepared;
  }

  @Override
  public String toString() {
    return "PgParamDesc{" +
      "paramDataTypes=" + Arrays.toString(paramDataTypes) +
      '}';
  }
}

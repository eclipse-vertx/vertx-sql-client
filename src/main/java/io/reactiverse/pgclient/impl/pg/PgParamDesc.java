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
package io.reactiverse.pgclient.impl.pg;

import io.reactiverse.pgclient.impl.ParamDesc;
import io.reactiverse.pgclient.impl.pg.util.Util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

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

  @Override
  public String prepare(List<Object> values) {
    if (values.size() != paramDataTypes.length) {
      return buildReport(values);
    }
    for (int i = 0;i < paramDataTypes.length;i++) {
      DataType paramDataType = paramDataTypes[i];
      Object value = values.get(i);
      Object val = DataTypeCodec.prepare(paramDataType, value);
      if (val != value) {
        if (val == DataTypeCodec.REFUSED_SENTINEL) {
          return buildReport(values);
        } else {
          values.set(i, val);
        }
      }
    }
    return null;
  }

  private String buildReport(List<Object> values) {
    return Util.buildInvalidArgsError(values.stream(), Stream.of(paramDataTypes).map(type -> type.decodingType));
  }

  @Override
  public String toString() {
    return "PgParamDesc{" +
      "paramDataTypes=" + Arrays.toString(paramDataTypes) +
      '}';
  }
}

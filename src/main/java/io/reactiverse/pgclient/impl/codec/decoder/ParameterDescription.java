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

package io.reactiverse.pgclient.impl.codec.decoder;

import io.reactiverse.pgclient.impl.codec.DataTypeCodec;
import io.reactiverse.pgclient.impl.codec.DataType;
import io.reactiverse.pgclient.impl.codec.util.Util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class ParameterDescription {

  // OIDs
  private final DataType[] paramDataTypes;

  public ParameterDescription(DataType[] paramDataTypes) {
    this.paramDataTypes = paramDataTypes;
  }

  public DataType[] getParamDataTypes() {
    return paramDataTypes;
  }

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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ParameterDescription that = (ParameterDescription) o;
    return Arrays.equals(paramDataTypes, that.paramDataTypes);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(paramDataTypes);
  }


  @Override
  public String toString() {
    return "ParameterDescription{" +
      "paramDataTypes=" + Arrays.toString(paramDataTypes) +
      '}';
  }
}

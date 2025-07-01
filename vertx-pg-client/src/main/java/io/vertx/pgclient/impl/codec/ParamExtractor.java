/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.pgclient.impl.codec;

import io.vertx.core.json.Json;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.internal.TupleBase;

import java.util.Arrays;
import java.util.stream.Collectors;

interface ParamExtractor<T> {

  static String extractUnknownType(TupleBase tuple, int pos) {
    Object value = tuple.getValue(pos);
    if (value instanceof Object[]) {
      String[] strings = tuple.getArrayOfStrings(pos);
      return Arrays.stream(strings).collect(Collectors.joining(",", "{", "}"));
    } else {
      return tuple.getString(pos);
    }
  }

  T get(TupleBase tuple, int idx);

  private static String encodeJsonToString(Object value) {
    if (value == Tuple.JSON_NULL) {
      return "null";
    } else {
      return Json.encode(value);
    }
  }

  static Object prepareUnknown(Object value) {
    return String.valueOf(value);
  }

  static Object prepareJson(Object value) {
    return encodeJsonToString(value);
  }

  static Object prepareNumeric(Object value) {
    assert value instanceof Number;
    return value.toString();
  }
}

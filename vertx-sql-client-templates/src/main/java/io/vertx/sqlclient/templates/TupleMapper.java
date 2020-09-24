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
package io.vertx.sqlclient.templates;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.templates.impl.JsonTuple;

import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * Map an arbitrary {@code I} object to a {@link Tuple}.
 * @param <I>
 */
@VertxGen
public interface TupleMapper<I> {

  /**
   * Create a mapper that associates a parameters object to a map of named parameters to
   * their respective value.
   *
   * @param fn the function turning a parameters object into a map
   * @return the mapper
   */
  static <T> TupleMapper<T> mapper(Function<T, Map<String, Object>> fn) {
    return (mapping, size, params) -> {
      Map<String, Object> args = fn.apply(params);
      Object[] array = new Object[size];
      for (int i = 0;i < array.length;i++) {
        String column = mapping.apply(i);
        array[i] = args.get(column);
      }
      return Tuple.wrap(array);
    };
  }

  /**
   * Map a {@link JsonObject} to a {@link Tuple}.
   */
  static TupleMapper<JsonObject> jsonObject() {
    return JsonTuple::new;
  }

  /**
   * The internal mapper contract that builds a tuple.
   *
   * @param mapping associates an index to template parameter name
   * @param size the tuple size
   * @param params the parameters object
   * @return the tuple
   */
  @GenIgnore
  Tuple map(IntFunction<String> mapping, int size, I params);

}

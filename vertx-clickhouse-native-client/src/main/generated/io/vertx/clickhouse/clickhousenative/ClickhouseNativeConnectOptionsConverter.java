/*
 *
 *  * Copyright (c) 2021 Vladimir Vishnevsky
 *  *
 *  * This program and the accompanying materials are made available under the
 *  * terms of the Eclipse Public License 2.0 which is available at
 *  * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 *  * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  *
 *  * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package io.vertx.clickhouse.clickhousenative;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter and mapper for {@link io.vertx.clickhouse.clickhousenative.ClickhouseNativeConnectOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.clickhouse.clickhousenative.ClickhouseNativeConnectOptions} original class using Vert.x codegen.
 */
public class ClickhouseNativeConnectOptionsConverter {


  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, ClickhouseNativeConnectOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
      }
    }
  }

  public static void toJson(ClickhouseNativeConnectOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(ClickhouseNativeConnectOptions obj, java.util.Map<String, Object> json) {
  }
}

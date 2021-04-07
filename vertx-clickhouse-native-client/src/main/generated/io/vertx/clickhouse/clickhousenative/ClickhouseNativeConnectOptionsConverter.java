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

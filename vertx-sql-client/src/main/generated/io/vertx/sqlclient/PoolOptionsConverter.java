package io.vertx.sqlclient;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter and mapper for {@link io.vertx.sqlclient.PoolOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.sqlclient.PoolOptions} original class using Vert.x codegen.
 */
public class PoolOptionsConverter {


  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, PoolOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "maxSize":
          if (member.getValue() instanceof Number) {
            obj.setMaxSize(((Number)member.getValue()).intValue());
          }
          break;
        case "maxWaitQueueSize":
          if (member.getValue() instanceof Number) {
            obj.setMaxWaitQueueSize(((Number)member.getValue()).intValue());
          }
          break;
      }
    }
  }

  public static void toJson(PoolOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(PoolOptions obj, java.util.Map<String, Object> json) {
    json.put("maxSize", obj.getMaxSize());
    json.put("maxWaitQueueSize", obj.getMaxWaitQueueSize());
  }
}

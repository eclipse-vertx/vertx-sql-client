package io.vertx.sqlclient;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter and mapper for {@link io.vertx.sqlclient.SqlHost}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.sqlclient.SqlHost} original class using Vert.x codegen.
 */
public class SqlHostConverter {


  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, SqlHost obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "host":
          if (member.getValue() instanceof String) {
            obj.setHost((String)member.getValue());
          }
          break;
        case "port":
          if (member.getValue() instanceof Number) {
            obj.setPort(((Number)member.getValue()).intValue());
          }
          break;
      }
    }
  }

  public static void toJson(SqlHost obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(SqlHost obj, java.util.Map<String, Object> json) {
    if (obj.getHost() != null) {
      json.put("host", obj.getHost());
    }
    json.put("port", obj.getPort());
  }
}

package io.vertx.pgclient;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter and mapper for {@link io.vertx.pgclient.PgConnectOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.pgclient.PgConnectOptions} original class using Vert.x codegen.
 */
public class PgConnectOptionsConverter {

   static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, PgConnectOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "pipeliningLimit":
          if (member.getValue() instanceof Number) {
            obj.setPipeliningLimit(((Number)member.getValue()).intValue());
          }
          break;
        case "sslMode":
          if (member.getValue() instanceof String) {
            obj.setSslMode(io.vertx.pgclient.SslMode.valueOf((String)member.getValue()));
          }
          break;
        case "useLayer7Proxy":
          if (member.getValue() instanceof Boolean) {
            obj.setUseLayer7Proxy((Boolean)member.getValue());
          }
          break;
      }
    }
  }

   static void toJson(PgConnectOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

   static void toJson(PgConnectOptions obj, java.util.Map<String, Object> json) {
    json.put("pipeliningLimit", obj.getPipeliningLimit());
    if (obj.getSslMode() != null) {
      json.put("sslMode", obj.getSslMode().name());
    }
    json.put("useLayer7Proxy", obj.getUseLayer7Proxy());
  }
}

package io.vertx.db2client;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter for {@link io.vertx.db2client.DB2ConnectOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.db2client.DB2ConnectOptions} original class using Vert.x codegen.
 */
public class DB2ConnectOptionsConverter {

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, DB2ConnectOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "pipeliningLimit":
          break;
      }
    }
  }

  public static void toJson(DB2ConnectOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(DB2ConnectOptions obj, java.util.Map<String, Object> json) {
    json.put("pipeliningLimit", obj.getPipeliningLimit());
  }
}

package io.vertx.sqlclient.transaction;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter and mapper for {@link io.vertx.sqlclient.transaction.TransactionOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.sqlclient.transaction.TransactionOptions} original class using Vert.x codegen.
 */
public class TransactionOptionsConverter {


  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, TransactionOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "accessMode":
          if (member.getValue() instanceof String) {
            obj.setAccessMode(io.vertx.sqlclient.transaction.TransactionAccessMode.valueOf((String)member.getValue()));
          }
          break;
        case "isolationLevel":
          if (member.getValue() instanceof String) {
            obj.setIsolationLevel(io.vertx.sqlclient.transaction.TransactionIsolationLevel.valueOf((String)member.getValue()));
          }
          break;
      }
    }
  }

  public static void toJson(TransactionOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(TransactionOptions obj, java.util.Map<String, Object> json) {
    if (obj.getAccessMode() != null) {
      json.put("accessMode", obj.getAccessMode().name());
    }
    if (obj.getIsolationLevel() != null) {
      json.put("isolationLevel", obj.getIsolationLevel().name());
    }
  }
}

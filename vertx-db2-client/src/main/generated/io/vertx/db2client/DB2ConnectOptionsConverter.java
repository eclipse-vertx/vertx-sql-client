package io.vertx.db2client;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.vertx.db2client.DB2ConnectOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.db2client.DB2ConnectOptions} original class using Vert.x codegen.
 */
public class DB2ConnectOptionsConverter {

  private static final Base64.Decoder BASE64_DECODER = Base64.getUrlDecoder();
  private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();

   static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, DB2ConnectOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "ssl":
          if (member.getValue() instanceof Boolean) {
            obj.setSsl((Boolean)member.getValue());
          }
          break;
        case "pipeliningLimit":
          break;
      }
    }
  }

   static void toJson(DB2ConnectOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

   static void toJson(DB2ConnectOptions obj, java.util.Map<String, Object> json) {
    json.put("ssl", obj.isSsl());
    json.put("pipeliningLimit", obj.getPipeliningLimit());
  }
}

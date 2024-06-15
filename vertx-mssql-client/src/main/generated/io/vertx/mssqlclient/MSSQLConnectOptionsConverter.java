package io.vertx.mssqlclient;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.vertx.mssqlclient.MSSQLConnectOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.mssqlclient.MSSQLConnectOptions} original class using Vert.x codegen.
 */
public class MSSQLConnectOptionsConverter {

  private static final Base64.Decoder BASE64_DECODER = Base64.getUrlDecoder();
  private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();

   static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, MSSQLConnectOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "packetSize":
          if (member.getValue() instanceof Number) {
            obj.setPacketSize(((Number)member.getValue()).intValue());
          }
          break;
        case "ssl":
          if (member.getValue() instanceof Boolean) {
            obj.setSsl((Boolean)member.getValue());
          }
          break;
      }
    }
  }

   static void toJson(MSSQLConnectOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

   static void toJson(MSSQLConnectOptions obj, java.util.Map<String, Object> json) {
    json.put("packetSize", obj.getPacketSize());
    json.put("ssl", obj.isSsl());
  }
}

package io.vertx.mysqlclient;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter for {@link io.vertx.mysqlclient.MySQLServerRsaPublicKeyOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.mysqlclient.MySQLServerRsaPublicKeyOptions} original class using Vert.x codegen.
 */
public class MySQLServerRsaPublicKeyOptionsConverter {

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, MySQLServerRsaPublicKeyOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "buffer":
          if (member.getValue() instanceof String) {
            obj.setBuffer(io.vertx.core.buffer.Buffer.buffer(java.util.Base64.getDecoder().decode((String)member.getValue())));
          }
          break;
        case "keyPath":
          if (member.getValue() instanceof String) {
            obj.setKeyPath((String)member.getValue());
          }
          break;
      }
    }
  }

  public static void toJson(MySQLServerRsaPublicKeyOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(MySQLServerRsaPublicKeyOptions obj, java.util.Map<String, Object> json) {
    if (obj.getBuffer() != null) {
      json.put("buffer", java.util.Base64.getEncoder().encodeToString(obj.getBuffer().getBytes()));
    }
    if (obj.getKeyPath() != null) {
      json.put("keyPath", obj.getKeyPath());
    }
  }
}

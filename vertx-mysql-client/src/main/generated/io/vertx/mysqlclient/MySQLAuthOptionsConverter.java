package io.vertx.mysqlclient;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter for {@link io.vertx.mysqlclient.MySQLAuthOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.mysqlclient.MySQLAuthOptions} original class using Vert.x codegen.
 */
public class MySQLAuthOptionsConverter {

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, MySQLAuthOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "charset":
          if (member.getValue() instanceof String) {
            obj.setCharset((String)member.getValue());
          }
          break;
        case "collation":
          if (member.getValue() instanceof String) {
            obj.setCollation((String)member.getValue());
          }
          break;
        case "database":
          if (member.getValue() instanceof String) {
            obj.setDatabase((String)member.getValue());
          }
          break;
        case "password":
          if (member.getValue() instanceof String) {
            obj.setPassword((String)member.getValue());
          }
          break;
        case "properties":
          if (member.getValue() instanceof JsonObject) {
            java.util.Map<String, java.lang.String> map = new java.util.LinkedHashMap<>();
            ((Iterable<java.util.Map.Entry<String, Object>>)member.getValue()).forEach(entry -> {
              if (entry.getValue() instanceof String)
                map.put(entry.getKey(), (String)entry.getValue());
            });
            obj.setProperties(map);
          }
          break;
        case "serverRsaPublicKeyPath":
          if (member.getValue() instanceof String) {
            obj.setServerRsaPublicKeyPath((String)member.getValue());
          }
          break;
        case "serverRsaPublicKeyValue":
          if (member.getValue() instanceof String) {
            obj.setServerRsaPublicKeyValue(io.vertx.core.buffer.Buffer.buffer(java.util.Base64.getDecoder().decode((String)member.getValue())));
          }
          break;
        case "user":
          if (member.getValue() instanceof String) {
            obj.setUser((String)member.getValue());
          }
          break;
      }
    }
  }

  public static void toJson(MySQLAuthOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(MySQLAuthOptions obj, java.util.Map<String, Object> json) {
    if (obj.getCharset() != null) {
      json.put("charset", obj.getCharset());
    }
    if (obj.getCollation() != null) {
      json.put("collation", obj.getCollation());
    }
    if (obj.getDatabase() != null) {
      json.put("database", obj.getDatabase());
    }
    if (obj.getPassword() != null) {
      json.put("password", obj.getPassword());
    }
    if (obj.getProperties() != null) {
      JsonObject map = new JsonObject();
      obj.getProperties().forEach((key, value) -> map.put(key, value));
      json.put("properties", map);
    }
    if (obj.getServerRsaPublicKeyPath() != null) {
      json.put("serverRsaPublicKeyPath", obj.getServerRsaPublicKeyPath());
    }
    if (obj.getServerRsaPublicKeyValue() != null) {
      json.put("serverRsaPublicKeyValue", java.util.Base64.getEncoder().encodeToString(obj.getServerRsaPublicKeyValue().getBytes()));
    }
    if (obj.getUser() != null) {
      json.put("user", obj.getUser());
    }
  }
}

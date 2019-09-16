package io.vertx.mysqlclient;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter for {@link io.vertx.mysqlclient.MySQLConnectOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.mysqlclient.MySQLConnectOptions} original class using Vert.x codegen.
 */
public class MySQLConnectOptionsConverter {

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, MySQLConnectOptions obj) {
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
        case "serverRsaPublicKeyOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setServerRsaPublicKeyOptions(new io.vertx.mysqlclient.MySQLServerRsaPublicKeyOptions((JsonObject)member.getValue()));
          }
          break;
        case "sslMode":
          if (member.getValue() instanceof String) {
            obj.setSslMode(io.vertx.mysqlclient.SslMode.valueOf((String)member.getValue()));
          }
          break;
      }
    }
  }

  public static void toJson(MySQLConnectOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(MySQLConnectOptions obj, java.util.Map<String, Object> json) {
    if (obj.getCharset() != null) {
      json.put("charset", obj.getCharset());
    }
    if (obj.getCollation() != null) {
      json.put("collation", obj.getCollation());
    }
    if (obj.getServerRsaPublicKeyOptions() != null) {
      json.put("serverRsaPublicKeyOptions", obj.getServerRsaPublicKeyOptions().toJson());
    }
    if (obj.getSslMode() != null) {
      json.put("sslMode", obj.getSslMode().name());
    }
  }
}

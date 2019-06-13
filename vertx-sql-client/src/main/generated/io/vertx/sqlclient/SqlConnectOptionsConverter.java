package io.vertx.sqlclient;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter for {@link io.vertx.sqlclient.SqlConnectOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.sqlclient.SqlConnectOptions} original class using Vert.x codegen.
 */
public class SqlConnectOptionsConverter {

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, SqlConnectOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "cachePreparedStatements":
          if (member.getValue() instanceof Boolean) {
            obj.setCachePreparedStatements((Boolean)member.getValue());
          }
          break;
        case "database":
          if (member.getValue() instanceof String) {
            obj.setDatabase((String)member.getValue());
          }
          break;
        case "host":
          if (member.getValue() instanceof String) {
            obj.setHost((String)member.getValue());
          }
          break;
        case "password":
          if (member.getValue() instanceof String) {
            obj.setPassword((String)member.getValue());
          }
          break;
        case "port":
          if (member.getValue() instanceof Number) {
            obj.setPort(((Number)member.getValue()).intValue());
          }
          break;
        case "preparedStatementCacheSize":
          if (member.getValue() instanceof Number) {
            obj.setPreparedStatementCacheSize(((Number)member.getValue()).intValue());
          }
          break;
        case "preparedStatementCacheSqlLimit":
          if (member.getValue() instanceof Number) {
            obj.setPreparedStatementCacheSqlLimit(((Number)member.getValue()).intValue());
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

  public static void toJson(SqlConnectOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(SqlConnectOptions obj, java.util.Map<String, Object> json) {
    json.put("cachePreparedStatements", obj.getCachePreparedStatements());
    if (obj.getDatabase() != null) {
      json.put("database", obj.getDatabase());
    }
    if (obj.getHost() != null) {
      json.put("host", obj.getHost());
    }
    if (obj.getPassword() != null) {
      json.put("password", obj.getPassword());
    }
    json.put("port", obj.getPort());
    json.put("preparedStatementCacheSize", obj.getPreparedStatementCacheSize());
    json.put("preparedStatementCacheSqlLimit", obj.getPreparedStatementCacheSqlLimit());
    if (obj.getUser() != null) {
      json.put("user", obj.getUser());
    }
  }
}

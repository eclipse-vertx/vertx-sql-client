package io.vertx.sqlclient;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter and mapper for {@link io.vertx.sqlclient.SqlConnectOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.sqlclient.SqlConnectOptions} original class using Vert.x codegen.
 */
public class SqlConnectOptionsConverter {

   static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, SqlConnectOptions obj) {
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
        case "user":
          if (member.getValue() instanceof String) {
            obj.setUser((String)member.getValue());
          }
          break;
        case "password":
          if (member.getValue() instanceof String) {
            obj.setPassword((String)member.getValue());
          }
          break;
        case "database":
          if (member.getValue() instanceof String) {
            obj.setDatabase((String)member.getValue());
          }
          break;
        case "cachePreparedStatements":
          if (member.getValue() instanceof Boolean) {
            obj.setCachePreparedStatements((Boolean)member.getValue());
          }
          break;
        case "preparedStatementCacheMaxSize":
          if (member.getValue() instanceof Number) {
            obj.setPreparedStatementCacheMaxSize(((Number)member.getValue()).intValue());
          }
          break;
        case "preparedStatementCacheSqlLimit":
          if (member.getValue() instanceof Number) {
            obj.setPreparedStatementCacheSqlLimit(((Number)member.getValue()).intValue());
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
        case "tracingPolicy":
          if (member.getValue() instanceof String) {
            obj.setTracingPolicy(io.vertx.core.tracing.TracingPolicy.valueOf((String)member.getValue()));
          }
          break;
        case "usingDomainSocket":
          break;
        case "reconnectAttempts":
          if (member.getValue() instanceof Number) {
            obj.setReconnectAttempts(((Number)member.getValue()).intValue());
          }
          break;
        case "reconnectInterval":
          if (member.getValue() instanceof Number) {
            obj.setReconnectInterval(((Number)member.getValue()).longValue());
          }
          break;
        case "metricsName":
          if (member.getValue() instanceof String) {
            obj.setMetricsName((String)member.getValue());
          }
          break;
        case "sslOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setSslOptions(new io.vertx.core.net.ClientSSLOptions((io.vertx.core.json.JsonObject)member.getValue()));
          }
          break;
      }
    }
  }

   static void toJson(SqlConnectOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

   static void toJson(SqlConnectOptions obj, java.util.Map<String, Object> json) {
    if (obj.getHost() != null) {
      json.put("host", obj.getHost());
    }
    json.put("port", obj.getPort());
    if (obj.getUser() != null) {
      json.put("user", obj.getUser());
    }
    if (obj.getPassword() != null) {
      json.put("password", obj.getPassword());
    }
    if (obj.getDatabase() != null) {
      json.put("database", obj.getDatabase());
    }
    json.put("cachePreparedStatements", obj.getCachePreparedStatements());
    json.put("preparedStatementCacheMaxSize", obj.getPreparedStatementCacheMaxSize());
    if (obj.getProperties() != null) {
      JsonObject map = new JsonObject();
      obj.getProperties().forEach((key, value) -> map.put(key, value));
      json.put("properties", map);
    }
    if (obj.getTracingPolicy() != null) {
      json.put("tracingPolicy", obj.getTracingPolicy().name());
    }
    json.put("usingDomainSocket", obj.isUsingDomainSocket());
    json.put("reconnectAttempts", obj.getReconnectAttempts());
    json.put("reconnectInterval", obj.getReconnectInterval());
    if (obj.getMetricsName() != null) {
      json.put("metricsName", obj.getMetricsName());
    }
    if (obj.getSslOptions() != null) {
      json.put("sslOptions", obj.getSslOptions().toJson());
    }
  }
}

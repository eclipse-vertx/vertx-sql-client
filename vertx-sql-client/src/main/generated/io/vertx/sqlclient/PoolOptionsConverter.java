package io.vertx.sqlclient;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter and mapper for {@link io.vertx.sqlclient.PoolOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.sqlclient.PoolOptions} original class using Vert.x codegen.
 */
public class PoolOptionsConverter {

   static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, PoolOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "maxSize":
          if (member.getValue() instanceof Number) {
            obj.setMaxSize(((Number)member.getValue()).intValue());
          }
          break;
        case "maxWaitQueueSize":
          if (member.getValue() instanceof Number) {
            obj.setMaxWaitQueueSize(((Number)member.getValue()).intValue());
          }
          break;
        case "idleTimeoutUnit":
          if (member.getValue() instanceof String) {
            obj.setIdleTimeoutUnit(java.util.concurrent.TimeUnit.valueOf((String)member.getValue()));
          }
          break;
        case "idleTimeout":
          if (member.getValue() instanceof Number) {
            obj.setIdleTimeout(((Number)member.getValue()).intValue());
          }
          break;
        case "maxLifetimeUnit":
          if (member.getValue() instanceof String) {
            obj.setMaxLifetimeUnit(java.util.concurrent.TimeUnit.valueOf((String)member.getValue()));
          }
          break;
        case "maxLifetime":
          if (member.getValue() instanceof Number) {
            obj.setMaxLifetime(((Number)member.getValue()).intValue());
          }
          break;
        case "jitter":
          if (member.getValue() instanceof Number) {
            obj.setJitter(((Number)member.getValue()).intValue());
          }
          break;
        case "poolCleanerPeriod":
          if (member.getValue() instanceof Number) {
            obj.setPoolCleanerPeriod(((Number)member.getValue()).intValue());
          }
          break;
        case "connectionTimeoutUnit":
          if (member.getValue() instanceof String) {
            obj.setConnectionTimeoutUnit(java.util.concurrent.TimeUnit.valueOf((String)member.getValue()));
          }
          break;
        case "connectionTimeout":
          if (member.getValue() instanceof Number) {
            obj.setConnectionTimeout(((Number)member.getValue()).intValue());
          }
          break;
        case "shared":
          if (member.getValue() instanceof Boolean) {
            obj.setShared((Boolean)member.getValue());
          }
          break;
        case "name":
          if (member.getValue() instanceof String) {
            obj.setName((String)member.getValue());
          }
          break;
        case "eventLoopSize":
          if (member.getValue() instanceof Number) {
            obj.setEventLoopSize(((Number)member.getValue()).intValue());
          }
          break;
      }
    }
  }

   static void toJson(PoolOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

   static void toJson(PoolOptions obj, java.util.Map<String, Object> json) {
    json.put("maxSize", obj.getMaxSize());
    json.put("maxWaitQueueSize", obj.getMaxWaitQueueSize());
    if (obj.getIdleTimeoutUnit() != null) {
      json.put("idleTimeoutUnit", obj.getIdleTimeoutUnit().name());
    }
    json.put("idleTimeout", obj.getIdleTimeout());
    if (obj.getMaxLifetimeUnit() != null) {
      json.put("maxLifetimeUnit", obj.getMaxLifetimeUnit().name());
    }
    json.put("maxLifetime", obj.getMaxLifetime());
    json.put("jitter", obj.getJitter());
    json.put("poolCleanerPeriod", obj.getPoolCleanerPeriod());
    if (obj.getConnectionTimeoutUnit() != null) {
      json.put("connectionTimeoutUnit", obj.getConnectionTimeoutUnit().name());
    }
    json.put("connectionTimeout", obj.getConnectionTimeout());
    json.put("shared", obj.isShared());
    if (obj.getName() != null) {
      json.put("name", obj.getName());
    }
    json.put("eventLoopSize", obj.getEventLoopSize());
  }
}

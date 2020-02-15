package io.vertx.pgclient;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter and mapper for {@link io.vertx.pgclient.PgNotification}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.pgclient.PgNotification} original class using Vert.x codegen.
 */
public class PgNotificationConverter {


  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, PgNotification obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "channel":
          if (member.getValue() instanceof String) {
            obj.setChannel((String)member.getValue());
          }
          break;
        case "payload":
          if (member.getValue() instanceof String) {
            obj.setPayload((String)member.getValue());
          }
          break;
        case "processId":
          if (member.getValue() instanceof Number) {
            obj.setProcessId(((Number)member.getValue()).intValue());
          }
          break;
      }
    }
  }

  public static PgNotification fromMap(Iterable<java.util.Map.Entry<String, Object>> map) {
    PgNotification obj = new PgNotification();
    fromMap(map, obj);
    return obj;
  }

  public static void fromMap(Iterable<java.util.Map.Entry<String, Object>> map, PgNotification obj) {
    for (java.util.Map.Entry<String, Object> member : map) {
      switch (member.getKey()) {
        case "channel":
          if (member.getValue() instanceof String) {
            obj.setChannel((String)member.getValue());
          }
          break;
        case "payload":
          if (member.getValue() instanceof String) {
            obj.setPayload((String)member.getValue());
          }
          break;
        case "processId":
          if (member.getValue() instanceof Number) {
            obj.setProcessId(((Number)member.getValue()).intValue());
          }
          break;
      }
    }
  }

  public static void toJson(PgNotification obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(PgNotification obj, java.util.Map<String, Object> json) {
    if (obj.getChannel() != null) {
      json.put("channel", obj.getChannel());
    }
    if (obj.getPayload() != null) {
      json.put("payload", obj.getPayload());
    }
    json.put("processId", obj.getProcessId());
  }
}

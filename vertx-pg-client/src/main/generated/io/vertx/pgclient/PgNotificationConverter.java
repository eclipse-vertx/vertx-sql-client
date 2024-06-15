package io.vertx.pgclient;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.vertx.pgclient.PgNotification}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.pgclient.PgNotification} original class using Vert.x codegen.
 */
public class PgNotificationConverter {

  private static final Base64.Decoder BASE64_DECODER = Base64.getUrlDecoder();
  private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();

   static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, PgNotification obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "processId":
          if (member.getValue() instanceof Number) {
            obj.setProcessId(((Number)member.getValue()).intValue());
          }
          break;
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
      }
    }
  }

   static void toJson(PgNotification obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

   static void toJson(PgNotification obj, java.util.Map<String, Object> json) {
    json.put("processId", obj.getProcessId());
    if (obj.getChannel() != null) {
      json.put("channel", obj.getChannel());
    }
    if (obj.getPayload() != null) {
      json.put("payload", obj.getPayload());
    }
  }
}

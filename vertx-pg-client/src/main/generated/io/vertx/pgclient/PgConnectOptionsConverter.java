package io.vertx.pgclient;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import io.vertx.core.spi.json.JsonCodec;

/**
 * Converter and Codec for {@link io.vertx.pgclient.PgConnectOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.pgclient.PgConnectOptions} original class using Vert.x codegen.
 */
public class PgConnectOptionsConverter implements JsonCodec<PgConnectOptions, JsonObject> {

  public static final PgConnectOptionsConverter INSTANCE = new PgConnectOptionsConverter();

  @Override public JsonObject encode(PgConnectOptions value) { return (value != null) ? value.toJson() : null; }

  @Override public PgConnectOptions decode(JsonObject value) { return (value != null) ? new PgConnectOptions(value) : null; }

  @Override public Class<PgConnectOptions> getTargetClass() { return PgConnectOptions.class; }

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, PgConnectOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "pipeliningLimit":
          if (member.getValue() instanceof Number) {
            obj.setPipeliningLimit(((Number)member.getValue()).intValue());
          }
          break;
        case "sslMode":
          if (member.getValue() instanceof String) {
            obj.setSslMode(io.vertx.pgclient.SslMode.valueOf((String)member.getValue()));
          }
          break;
        case "usingDomainSocket":
          break;
      }
    }
  }

  public static void toJson(PgConnectOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(PgConnectOptions obj, java.util.Map<String, Object> json) {
    json.put("pipeliningLimit", obj.getPipeliningLimit());
    if (obj.getSslMode() != null) {
      json.put("sslMode", obj.getSslMode().name());
    }
    json.put("usingDomainSocket", obj.isUsingDomainSocket());
  }
}

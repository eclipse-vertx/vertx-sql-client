package io.vertx.mysqlclient;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import io.vertx.core.spi.json.JsonCodec;

/**
 * Converter and Codec for {@link io.vertx.mysqlclient.MySQLConnectOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.mysqlclient.MySQLConnectOptions} original class using Vert.x codegen.
 */
public class MySQLConnectOptionsConverter implements JsonCodec<MySQLConnectOptions, JsonObject> {

  public static final MySQLConnectOptionsConverter INSTANCE = new MySQLConnectOptionsConverter();

  @Override public JsonObject encode(MySQLConnectOptions value) { return (value != null) ? value.toJson() : null; }

  @Override public MySQLConnectOptions decode(JsonObject value) { return (value != null) ? new MySQLConnectOptions(value) : null; }

  @Override public Class<MySQLConnectOptions> getTargetClass() { return MySQLConnectOptions.class; }

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
  }
}

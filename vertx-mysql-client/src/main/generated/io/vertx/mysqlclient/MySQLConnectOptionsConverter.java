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
      }
    }
  }

  public static void toJson(MySQLConnectOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(MySQLConnectOptions obj, java.util.Map<String, Object> json) {
  }
}

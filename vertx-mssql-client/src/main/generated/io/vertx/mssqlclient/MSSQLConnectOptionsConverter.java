package io.vertx.mssqlclient;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter and mapper for {@link io.vertx.mssqlclient.MSSQLConnectOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.mssqlclient.MSSQLConnectOptions} original class using Vert.x codegen.
 */
public class MSSQLConnectOptionsConverter {


  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, MSSQLConnectOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
      }
    }
  }

  public static MSSQLConnectOptions fromMap(Iterable<java.util.Map.Entry<String, Object>> map) {
    MSSQLConnectOptions obj = new MSSQLConnectOptions();
    fromMap(map, obj);
    return obj;
  }

  public static void fromMap(Iterable<java.util.Map.Entry<String, Object>> map, MSSQLConnectOptions obj) {
    for (java.util.Map.Entry<String, Object> member : map) {
      switch (member.getKey()) {
      }
    }
  }

  public static void toJson(MSSQLConnectOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(MSSQLConnectOptions obj, java.util.Map<String, Object> json) {
  }
}

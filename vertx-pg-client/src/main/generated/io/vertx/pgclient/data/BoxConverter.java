package io.vertx.pgclient.data;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter and mapper for {@link io.vertx.pgclient.data.Box}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.pgclient.data.Box} original class using Vert.x codegen.
 */
public class BoxConverter {


  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, Box obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "lowerLeftCorner":
          if (member.getValue() instanceof JsonObject) {
            obj.setLowerLeftCorner(new io.vertx.pgclient.data.Point((JsonObject)member.getValue()));
          }
          break;
        case "upperRightCorner":
          if (member.getValue() instanceof JsonObject) {
            obj.setUpperRightCorner(new io.vertx.pgclient.data.Point((JsonObject)member.getValue()));
          }
          break;
      }
    }
  }

  public static Box fromMap(Iterable<java.util.Map.Entry<String, Object>> map) {
    Box obj = new Box();
    fromMap(map, obj);
    return obj;
  }

  public static void fromMap(Iterable<java.util.Map.Entry<String, Object>> map, Box obj) {
    for (java.util.Map.Entry<String, Object> member : map) {
      switch (member.getKey()) {
        case "lowerLeftCorner":
          if (member.getValue() instanceof io.vertx.pgclient.data.Point) {
            obj.setLowerLeftCorner((io.vertx.pgclient.data.Point)member.getValue());
          }
          break;
        case "upperRightCorner":
          if (member.getValue() instanceof io.vertx.pgclient.data.Point) {
            obj.setUpperRightCorner((io.vertx.pgclient.data.Point)member.getValue());
          }
          break;
      }
    }
  }

  public static void toJson(Box obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(Box obj, java.util.Map<String, Object> json) {
    if (obj.getLowerLeftCorner() != null) {
      json.put("lowerLeftCorner", obj.getLowerLeftCorner().toJson());
    }
    if (obj.getUpperRightCorner() != null) {
      json.put("upperRightCorner", obj.getUpperRightCorner().toJson());
    }
  }
}

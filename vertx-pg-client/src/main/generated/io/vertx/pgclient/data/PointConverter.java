package io.vertx.pgclient.data;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter for {@link io.vertx.pgclient.data.Point}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.pgclient.data.Point} original class using Vert.x codegen.
 */
public class PointConverter {

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, Point obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "x":
          if (member.getValue() instanceof Number) {
            obj.setX(((Number)member.getValue()).doubleValue());
          }
          break;
        case "y":
          if (member.getValue() instanceof Number) {
            obj.setY(((Number)member.getValue()).doubleValue());
          }
          break;
      }
    }
  }

  public static void toJson(Point obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(Point obj, java.util.Map<String, Object> json) {
    json.put("x", obj.getX());
    json.put("y", obj.getY());
  }
}

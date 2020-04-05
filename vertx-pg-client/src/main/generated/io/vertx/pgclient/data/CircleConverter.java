package io.vertx.pgclient.data;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter and mapper for {@link io.vertx.pgclient.data.Circle}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.pgclient.data.Circle} original class using Vert.x codegen.
 */
public class CircleConverter {


  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, Circle obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "centerPoint":
          if (member.getValue() instanceof JsonObject) {
            obj.setCenterPoint(new io.vertx.pgclient.data.Point((io.vertx.core.json.JsonObject)member.getValue()));
          }
          break;
        case "radius":
          if (member.getValue() instanceof Number) {
            obj.setRadius(((Number)member.getValue()).doubleValue());
          }
          break;
      }
    }
  }

  public static void toJson(Circle obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(Circle obj, java.util.Map<String, Object> json) {
    if (obj.getCenterPoint() != null) {
      json.put("centerPoint", obj.getCenterPoint().toJson());
    }
    json.put("radius", obj.getRadius());
  }
}

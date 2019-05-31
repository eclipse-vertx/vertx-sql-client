package io.vertx.pgclient.data;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter for {@link io.vertx.pgclient.data.LineSegment}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.pgclient.data.LineSegment} original class using Vert.x codegen.
 */
public class LineSegmentConverter {

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, LineSegment obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "p1":
          if (member.getValue() instanceof JsonObject) {
            obj.setP1(new io.vertx.pgclient.data.Point((JsonObject)member.getValue()));
          }
          break;
        case "p2":
          if (member.getValue() instanceof JsonObject) {
            obj.setP2(new io.vertx.pgclient.data.Point((JsonObject)member.getValue()));
          }
          break;
      }
    }
  }

  public static void toJson(LineSegment obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(LineSegment obj, java.util.Map<String, Object> json) {
    if (obj.getP1() != null) {
      json.put("p1", obj.getP1().toJson());
    }
    if (obj.getP2() != null) {
      json.put("p2", obj.getP2().toJson());
    }
  }
}

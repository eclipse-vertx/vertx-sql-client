package io.vertx.pgclient.data;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter and mapper for {@link io.vertx.pgclient.data.Line}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.pgclient.data.Line} original class using Vert.x codegen.
 */
public class LineConverter {


  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, Line obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "a":
          if (member.getValue() instanceof Number) {
            obj.setA(((Number)member.getValue()).doubleValue());
          }
          break;
        case "b":
          if (member.getValue() instanceof Number) {
            obj.setB(((Number)member.getValue()).doubleValue());
          }
          break;
        case "c":
          if (member.getValue() instanceof Number) {
            obj.setC(((Number)member.getValue()).doubleValue());
          }
          break;
      }
    }
  }

  public static void toJson(Line obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(Line obj, java.util.Map<String, Object> json) {
    json.put("a", obj.getA());
    json.put("b", obj.getB());
    json.put("c", obj.getC());
  }
}

package io.vertx.pgclient.data;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter and mapper for {@link io.vertx.pgclient.data.Polygon}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.pgclient.data.Polygon} original class using Vert.x codegen.
 */
public class PolygonConverter {


  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, Polygon obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "points":
          if (member.getValue() instanceof JsonArray) {
            java.util.ArrayList<io.vertx.pgclient.data.Point> list =  new java.util.ArrayList<>();
            ((Iterable<Object>)member.getValue()).forEach( item -> {
              if (item instanceof JsonObject)
                list.add(new io.vertx.pgclient.data.Point((JsonObject)item));
            });
            obj.setPoints(list);
          }
          break;
      }
    }
  }

  public static Polygon fromMap(Iterable<java.util.Map.Entry<String, Object>> map) {
    Polygon obj = new Polygon();
    fromMap(map, obj);
    return obj;
  }

  public static void fromMap(Iterable<java.util.Map.Entry<String, Object>> map, Polygon obj) {
    for (java.util.Map.Entry<String, Object> member : map) {
      switch (member.getKey()) {
      }
    }
  }

  public static void toJson(Polygon obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(Polygon obj, java.util.Map<String, Object> json) {
    if (obj.getPoints() != null) {
      JsonArray array = new JsonArray();
      obj.getPoints().forEach(item -> array.add(item.toJson()));
      json.put("points", array);
    }
  }
}

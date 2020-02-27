package io.vertx.pgclient.data;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter and mapper for {@link io.vertx.pgclient.data.Path}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.pgclient.data.Path} original class using Vert.x codegen.
 */
public class PathConverter {


  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, Path obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "open":
          if (member.getValue() instanceof Boolean) {
            obj.setOpen((Boolean)member.getValue());
          }
          break;
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

  public static void toJson(Path obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(Path obj, java.util.Map<String, Object> json) {
    json.put("open", obj.isOpen());
    if (obj.getPoints() != null) {
      JsonArray array = new JsonArray();
      obj.getPoints().forEach(item -> array.add(item.toJson()));
      json.put("points", array);
    }
  }
}

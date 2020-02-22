package io.vertx.mysqlclient.data.spatial;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter and mapper for {@link io.vertx.mysqlclient.data.spatial.LineString}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.mysqlclient.data.spatial.LineString} original class using Vert.x codegen.
 */
public class LineStringConverter {


  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, LineString obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "points":
          if (member.getValue() instanceof JsonArray) {
            java.util.ArrayList<io.vertx.mysqlclient.data.spatial.Point> list =  new java.util.ArrayList<>();
            ((Iterable<Object>)member.getValue()).forEach( item -> {
              if (item instanceof JsonObject)
                list.add(new io.vertx.mysqlclient.data.spatial.Point((JsonObject)item));
            });
            obj.setPoints(list);
          }
          break;
      }
    }
  }

  public static void toJson(LineString obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(LineString obj, java.util.Map<String, Object> json) {
    if (obj.getPoints() != null) {
      JsonArray array = new JsonArray();
      obj.getPoints().forEach(item -> array.add(item.toJson()));
      json.put("points", array);
    }
  }
}

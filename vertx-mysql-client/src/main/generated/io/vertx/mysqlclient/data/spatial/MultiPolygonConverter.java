package io.vertx.mysqlclient.data.spatial;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter and mapper for {@link io.vertx.mysqlclient.data.spatial.MultiPolygon}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.mysqlclient.data.spatial.MultiPolygon} original class using Vert.x codegen.
 */
public class MultiPolygonConverter {


  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, MultiPolygon obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "polygons":
          if (member.getValue() instanceof JsonArray) {
            java.util.ArrayList<io.vertx.mysqlclient.data.spatial.Polygon> list =  new java.util.ArrayList<>();
            ((Iterable<Object>)member.getValue()).forEach( item -> {
              if (item instanceof JsonObject)
                list.add(new io.vertx.mysqlclient.data.spatial.Polygon((io.vertx.core.json.JsonObject)item));
            });
            obj.setPolygons(list);
          }
          break;
      }
    }
  }

  public static void toJson(MultiPolygon obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(MultiPolygon obj, java.util.Map<String, Object> json) {
    if (obj.getPolygons() != null) {
      JsonArray array = new JsonArray();
      obj.getPolygons().forEach(item -> array.add(item.toJson()));
      json.put("polygons", array);
    }
  }
}

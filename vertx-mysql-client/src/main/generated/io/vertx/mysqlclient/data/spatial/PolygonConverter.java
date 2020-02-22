package io.vertx.mysqlclient.data.spatial;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter and mapper for {@link io.vertx.mysqlclient.data.spatial.Polygon}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.mysqlclient.data.spatial.Polygon} original class using Vert.x codegen.
 */
public class PolygonConverter {


  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, Polygon obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "lineStrings":
          if (member.getValue() instanceof JsonArray) {
            java.util.ArrayList<io.vertx.mysqlclient.data.spatial.LineString> list =  new java.util.ArrayList<>();
            ((Iterable<Object>)member.getValue()).forEach( item -> {
              if (item instanceof JsonObject)
                list.add(new io.vertx.mysqlclient.data.spatial.LineString((JsonObject)item));
            });
            obj.setLineStrings(list);
          }
          break;
      }
    }
  }

  public static void toJson(Polygon obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(Polygon obj, java.util.Map<String, Object> json) {
    if (obj.getLineStrings() != null) {
      JsonArray array = new JsonArray();
      obj.getLineStrings().forEach(item -> array.add(item.toJson()));
      json.put("lineStrings", array);
    }
  }
}

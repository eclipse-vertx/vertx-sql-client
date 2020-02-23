package io.vertx.mysqlclient.data.spatial;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter and mapper for {@link io.vertx.mysqlclient.data.spatial.Geometry}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.mysqlclient.data.spatial.Geometry} original class using Vert.x codegen.
 */
public class GeometryConverter {


  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, Geometry obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "srid":
          if (member.getValue() instanceof Number) {
            obj.setSRID(((Number)member.getValue()).longValue());
          }
          break;
      }
    }
  }

  public static void toJson(Geometry obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(Geometry obj, java.util.Map<String, Object> json) {
    json.put("srid", obj.getSRID());
  }
}

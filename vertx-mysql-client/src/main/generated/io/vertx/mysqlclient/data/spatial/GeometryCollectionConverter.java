package io.vertx.mysqlclient.data.spatial;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter and mapper for {@link io.vertx.mysqlclient.data.spatial.GeometryCollection}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.mysqlclient.data.spatial.GeometryCollection} original class using Vert.x codegen.
 */
public class GeometryCollectionConverter {


  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, GeometryCollection obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
      }
    }
  }

  public static void toJson(GeometryCollection obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(GeometryCollection obj, java.util.Map<String, Object> json) {
    if (obj.getGeometries() != null) {
      JsonArray array = new JsonArray();
      obj.getGeometries().forEach(item -> array.add(item.toJson()));
      json.put("geometries", array);
    }
  }
}

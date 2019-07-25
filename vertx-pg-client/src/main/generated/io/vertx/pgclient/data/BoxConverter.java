package io.vertx.pgclient.data;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import io.vertx.core.spi.json.JsonCodec;

/**
 * Converter and Codec for {@link io.vertx.pgclient.data.Box}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.pgclient.data.Box} original class using Vert.x codegen.
 */
public class BoxConverter implements JsonCodec<Box, JsonObject> {

  public static final BoxConverter INSTANCE = new BoxConverter();

  @Override public JsonObject encode(Box value) { return (value != null) ? value.toJson() : null; }

  @Override public Box decode(JsonObject value) { return (value != null) ? new Box(value) : null; }

  @Override public Class<Box> getTargetClass() { return Box.class; }

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, Box obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "lowerLeftCorner":
          if (member.getValue() instanceof JsonObject) {
            obj.setLowerLeftCorner(new io.vertx.pgclient.data.Point((JsonObject)member.getValue()));
          }
          break;
        case "upperRightCorner":
          if (member.getValue() instanceof JsonObject) {
            obj.setUpperRightCorner(new io.vertx.pgclient.data.Point((JsonObject)member.getValue()));
          }
          break;
      }
    }
  }

  public static void toJson(Box obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(Box obj, java.util.Map<String, Object> json) {
    if (obj.getLowerLeftCorner() != null) {
      json.put("lowerLeftCorner", obj.getLowerLeftCorner().toJson());
    }
    if (obj.getUpperRightCorner() != null) {
      json.put("upperRightCorner", obj.getUpperRightCorner().toJson());
    }
  }
}

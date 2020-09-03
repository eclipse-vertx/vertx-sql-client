package io.vertx.sqlclient.impl;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static io.vertx.sqlclient.Tuple.JSON_NULL;

/**
 * Bunch of generic utils.
 */
public final class Utils {

  private Utils() {
  }

  public static Object toJson(Object value) {
    if (value == null || value == JSON_NULL) {
      return null;
    } else if (value instanceof String
      || value instanceof Boolean
      || value instanceof Number
      || value instanceof Buffer
      || value instanceof JsonObject
      || value instanceof JsonArray) {
      return value;
    } else if (value.getClass().isArray()) {
      int len = Array.getLength(value);
      JsonArray array = new JsonArray(new ArrayList<>(len));
      for (int idx = 0;idx < len;idx++) {
        Object component = toJson(Array.get(value, idx));
        array.add(component);
      }
      return array;
    } else {
      return value.toString();
    }
  }
}

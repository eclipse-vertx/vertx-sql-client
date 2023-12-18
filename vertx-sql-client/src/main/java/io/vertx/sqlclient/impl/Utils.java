package io.vertx.sqlclient.impl;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.Array;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

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
    }
    if (value instanceof String
      || value instanceof Boolean
      || value instanceof Number
      || value instanceof Buffer
      || value instanceof JsonObject
      || value instanceof JsonArray) {
      return value;
    }
    if (value.getClass().isArray()) {
      int len = Array.getLength(value);
      JsonArray array = new JsonArray(new ArrayList<>(len));
      for (int idx = 0; idx < len; idx++) {
        Object component = toJson(Array.get(value, idx));
        array.add(component);
      }
      return array;
    }
    if (value instanceof Temporal) {
      Temporal temporal = (Temporal) value;
      if (temporal.isSupported(ChronoField.INSTANT_SECONDS)) {
        return DateTimeFormatter.ISO_INSTANT.format(temporal);
      }
    }
    return value.toString();
  }

  public static <T> Supplier<Future<T>> roundRobinSupplier(List<T> factories) {
    return new Supplier<Future<T>>() {
      final AtomicLong idx = new AtomicLong();
      @Override
      public Future<T> get() {
        long val = idx.getAndIncrement();
        T f = factories.get((int)val % factories.size());
        return Future.succeededFuture(f);
      }
    };
  }

  public static <T> Supplier<Future<T>> singletonSupplier(T factory) {
    return () -> Future.succeededFuture(factory);
  }
}

package io.vertx.sqlclient;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

interface ChainConverter<T> {

  static <T> ChainConverter<T> create(Class<T> tClass) {
    return new ChainConverterImpl<>(tClass);
  }

  static <T> ChainConverter<T> allowNull(Class<T> tClass) {
    return create(tClass).orNext(o -> {
      if (o == null) return null;
      throw new RuntimeException();
    });
  }

  static <T> ChainConverter<T> allowCast(Class<T> tClass) {
    return create(tClass).orNext(tClass::cast);
  }

  static ChainConverter<JsonObject> allowJsonObject() {
    return ChainConverter.allowCast(JsonObject.class).orNext(o -> new JsonObject((Map<String, Object>) o));
  }

  static ChainConverter<JsonArray> allowJsonArray() {
    return ChainConverter.allowCast(JsonArray.class)
      .orNext(o -> JsonArray.of(((Collection<?>) o).toArray()))
      .orNext(ChainConverterImpl::arrayToJsonArray);
  }

  static ChainConverter<Object> allowJson() {
    return allowNull(Object.class)
      .orNext(o -> {
        if (o == Tuple.JSON_NULL) return Tuple.JSON_NULL;
        throw new RuntimeException();
      })
      .orNext(JsonObject.class::cast)
      .orNext(JsonArray.class::cast)
      .orNext(Number.class::cast)
      .orNext(Boolean.class::cast)
      .orNext(String.class::cast);
  }

  ChainConverter<T> orNext(Function<Object, T> next);

  T apply(Object o);

  T[] toArray(Object o);

  class ChainConverterImpl<T> implements ChainConverter<T> {
    private final Class<T> tClass;
    private final List<Function<Object, T>> nextFunctions = new ArrayList<>();

    ChainConverterImpl(Class<T> tClass) {
      this.tClass = tClass;
    }

    @Override
    public ChainConverter<T> orNext(Function<Object, T> next) {
      this.nextFunctions.add(next);
      return this;
    }

    @Override
    public T apply(Object o) {
      for (Function<Object, T> next : nextFunctions) {
        try {
          return next.apply(o);
        } catch (Exception ignored) {
        }
      }
      throw new ClassCastException("Cannot cast " + (o == null ? null : o.getClass().getName()) + " to " + tClass.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public T[] toArray(Object val) {
      if (val == null) return null;
      if (!val.getClass().isArray()) {
        throw new ClassCastException("Invalid " + tClass.getSimpleName() + " array value type " + val.getClass());
      }
      final Class<?> componentType = val.getClass().getComponentType();
      if (this.tClass.isAssignableFrom(componentType)) {
        return (T[]) val;
      }
      final int len = Array.getLength(val);
      final T[] arr = (T[]) Array.newInstance(tClass, len);

      for (int i = 0; i < len; i++) {
        try {
          arr[i] = apply(Array.get(val, i));
        } catch (Exception e) {
          throw new ClassCastException("Invalid " + tClass.getSimpleName() + " array value type " + val.getClass());
        }
      }
      return arr;
    }

    static JsonArray arrayToJsonArray(Object o) {
      if (o.getClass().isArray() && !Buffer.class.isAssignableFrom(o.getClass().getComponentType())) {
        JsonArray array = new JsonArray();
        for (int i = 0; i < Array.getLength(o); i++) {
          array.add(Array.get(o, i));
        }
        return array;
      }
      throw new RuntimeException();
    }
  }
}

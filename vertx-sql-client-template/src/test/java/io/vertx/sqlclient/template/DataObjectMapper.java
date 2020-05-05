package io.vertx.sqlclient.template;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.template.wrappers.BooleanWrapper;
import io.vertx.sqlclient.template.wrappers.DoubleWrapper;
import io.vertx.sqlclient.template.wrappers.FloatWrapper;
import io.vertx.sqlclient.template.wrappers.IntegerWrapper;
import io.vertx.sqlclient.template.wrappers.JsonArrayWrapper;
import io.vertx.sqlclient.template.wrappers.JsonObjectWrapper;
import io.vertx.sqlclient.template.wrappers.LongWrapper;
import io.vertx.sqlclient.template.wrappers.ShortWrapper;
import io.vertx.sqlclient.template.wrappers.StringWrapper;

public class DataObjectMapper {

  public static BooleanWrapper toBoolean(Boolean v) {
    return new BooleanWrapper(v);
  }

  public static Boolean fromBoolean(BooleanWrapper v) {
    return v.get();
  }

  public static ShortWrapper toShort(Short v) {
    return new ShortWrapper(v);
  }

  public static Short fromShort(ShortWrapper v) {
    return v.get();
  }

  public static IntegerWrapper toInteger(Integer v) {
    return new IntegerWrapper(v);
  }

  public static Integer fromInteger(IntegerWrapper v) {
    return v.get();
  }

  public static LongWrapper toLong(Long v) {
    return new LongWrapper(v);
  }

  public static Long fromLong(LongWrapper v) {
    return v.get();
  }

  public static FloatWrapper toFloat(Float v) {
    return new FloatWrapper(v);
  }

  public static Float fromFloat(FloatWrapper v) {
    return v.get();
  }

  public static DoubleWrapper toDouble(Double v) {
    return new DoubleWrapper(v);
  }

  public static Double fromDouble(DoubleWrapper v) {
    return v.get();
  }

  public static StringWrapper toString(String s) {
    return new StringWrapper(s);
  }

  public static String fromString(StringWrapper v) {
    return v.get();
  }

  public static JsonObjectWrapper toJsonObject(JsonObject s) {
    return new JsonObjectWrapper(s);
  }

  public static JsonObject fromJsonObject(JsonObjectWrapper v) {
    return v.get();
  }

  public static JsonArrayWrapper toJsonArray(JsonArray s) {
    return new JsonArrayWrapper(s);
  }

  public static JsonArray fromJsonArray(JsonArrayWrapper v) {
    return v.get();
  }
}

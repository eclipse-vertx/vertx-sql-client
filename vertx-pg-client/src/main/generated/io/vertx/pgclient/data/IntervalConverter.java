package io.vertx.pgclient.data;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import io.vertx.core.spi.json.JsonCodec;

/**
 * Converter and Codec for {@link io.vertx.pgclient.data.Interval}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.pgclient.data.Interval} original class using Vert.x codegen.
 */
public class IntervalConverter implements JsonCodec<Interval, JsonObject> {

  public static final IntervalConverter INSTANCE = new IntervalConverter();

  @Override public JsonObject encode(Interval value) { return (value != null) ? value.toJson() : null; }

  @Override public Interval decode(JsonObject value) { return (value != null) ? new Interval(value) : null; }

  @Override public Class<Interval> getTargetClass() { return Interval.class; }

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, Interval obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "days":
          if (member.getValue() instanceof Number) {
            obj.setDays(((Number)member.getValue()).intValue());
          }
          break;
        case "hours":
          if (member.getValue() instanceof Number) {
            obj.setHours(((Number)member.getValue()).intValue());
          }
          break;
        case "microseconds":
          if (member.getValue() instanceof Number) {
            obj.setMicroseconds(((Number)member.getValue()).intValue());
          }
          break;
        case "minutes":
          if (member.getValue() instanceof Number) {
            obj.setMinutes(((Number)member.getValue()).intValue());
          }
          break;
        case "months":
          if (member.getValue() instanceof Number) {
            obj.setMonths(((Number)member.getValue()).intValue());
          }
          break;
        case "seconds":
          if (member.getValue() instanceof Number) {
            obj.setSeconds(((Number)member.getValue()).intValue());
          }
          break;
        case "years":
          if (member.getValue() instanceof Number) {
            obj.setYears(((Number)member.getValue()).intValue());
          }
          break;
      }
    }
  }

  public static void toJson(Interval obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(Interval obj, java.util.Map<String, Object> json) {
    json.put("days", obj.getDays());
    json.put("hours", obj.getHours());
    json.put("microseconds", obj.getMicroseconds());
    json.put("minutes", obj.getMinutes());
    json.put("months", obj.getMonths());
    json.put("seconds", obj.getSeconds());
    json.put("years", obj.getYears());
  }
}

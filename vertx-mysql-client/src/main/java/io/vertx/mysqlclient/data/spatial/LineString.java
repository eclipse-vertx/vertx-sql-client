package io.vertx.mysqlclient.data.spatial;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A LineString is a Curve with linear interpolation between points, it may represents a Line or a LinearRing.
 */
@DataObject(generateConverter = true)
public class LineString extends Geometry {
  private List<Point> points;

  public LineString() {
  }

  public LineString(JsonObject json) {
    super(json);
    LineStringConverter.fromJson(json, this);
  }

  public LineString(LineString other) {
    super(other);
    this.points = new ArrayList<>(other.points);
  }

  public LineString(long SRID, List<Point> points) {
    super(SRID);
    this.points = points;
  }

  public LineString setPoints(List<Point> points) {
    this.points = points;
    return this;
  }

  public List<Point> getPoints() {
    return points;
  }

  @Override
  public JsonObject toJson() {
    JsonObject json = super.toJson();
    LineStringConverter.toJson(this, json);
    return json;
  }
}

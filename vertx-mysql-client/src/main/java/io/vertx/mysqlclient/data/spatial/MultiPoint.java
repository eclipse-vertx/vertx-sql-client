package io.vertx.mysqlclient.data.spatial;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A MultiPoint is a geometry collection composed of Point elements. The points are not connected or ordered in any way.
 */
@DataObject(generateConverter = true)
public class MultiPoint extends Geometry {
  private List<Point> points;

  public MultiPoint() {
  }

  public MultiPoint(JsonObject json) {
    super(json);
    MultiPointConverter.fromJson(json, this);
  }

  public MultiPoint(MultiPoint other) {
    super(other);
    this.points = new ArrayList<>(other.points);
  }

  public MultiPoint(long SRID, List<Point> points) {
    super(SRID);
    this.points = points;
  }

  public MultiPoint setPoints(List<Point> points) {
    this.points = points;
    return this;
  }

  public List<Point> getPoints() {
    return points;
  }

  @Override
  public JsonObject toJson() {
    JsonObject json = super.toJson();
    MultiPointConverter.toJson(this, json);
    return json;
  }
}

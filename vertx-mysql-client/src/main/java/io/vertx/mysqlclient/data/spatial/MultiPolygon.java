package io.vertx.mysqlclient.data.spatial;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A MultiPolygon is a MultiSurface object composed of Polygon elements.
 */
@DataObject(generateConverter = true)
public class MultiPolygon extends Geometry {
  private List<Polygon> polygons;

  public MultiPolygon() {
  }

  public MultiPolygon(JsonObject json) {
    super(json);
    MultiPolygonConverter.fromJson(json, this);
  }

  public MultiPolygon(MultiPolygon other) {
    super(other);
    this.polygons = new ArrayList<>(other.polygons);
  }

  public MultiPolygon(long SRID, List<Polygon> polygons) {
    super(SRID);
    this.polygons = polygons;
  }

  public List<Polygon> getPolygons() {
    return polygons;
  }

  public MultiPolygon setPolygons(List<Polygon> polygons) {
    this.polygons = polygons;
    return this;
  }

  @Override
  public JsonObject toJson() {
    JsonObject json = super.toJson();
    MultiPolygonConverter.toJson(this, json);
    return json;
  }
}

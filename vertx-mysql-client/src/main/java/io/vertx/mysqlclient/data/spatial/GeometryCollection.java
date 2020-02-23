package io.vertx.mysqlclient.data.spatial;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A GeomCollection is a geometry that is a collection of zero or more geometries of any class.
 */
@DataObject(generateConverter = true)
public class GeometryCollection extends Geometry {
  private List<Geometry> geometries;

  public GeometryCollection() {
  }

  public GeometryCollection(JsonObject json) {
    super(json);
    GeometryCollectionConverter.fromJson(json, this);
  }

  public GeometryCollection(GeometryCollection other) {
    super(other);
    this.geometries = new ArrayList<>(other.geometries);
  }

  public GeometryCollection(long SRID, List<Geometry> geometries) {
    super(SRID);
    this.geometries = geometries;
  }

  public GeometryCollection setGeometries(List<Geometry> geometries) {
    this.geometries = geometries;
    return this;
  }

  public List<Geometry> getGeometries() {
    return geometries;
  }

  @Override
  public JsonObject toJson() {
    JsonObject json = super.toJson();
    GeometryCollectionConverter.toJson(this, json);
    return json;
  }
}

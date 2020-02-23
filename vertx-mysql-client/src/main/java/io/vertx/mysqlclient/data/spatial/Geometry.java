package io.vertx.mysqlclient.data.spatial;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * Geometry is an abstract class which represents the base of MySQL geometry data type.
 */
@DataObject(generateConverter = true)
public abstract class Geometry {
  private long SRID;

  public Geometry() {
  }

  public Geometry(JsonObject json) {
    GeometryConverter.fromJson(json, this);
  }

  public Geometry(Geometry other) {
    this.SRID = other.SRID;
  }

  public Geometry(long SRID) {
    this.SRID = SRID;
  }

  public long getSRID() {
    return SRID;
  }

  public Geometry setSRID(long SRID) {
    this.SRID = SRID;
    return this;
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    GeometryConverter.toJson(this, json);
    return json;
  }
}

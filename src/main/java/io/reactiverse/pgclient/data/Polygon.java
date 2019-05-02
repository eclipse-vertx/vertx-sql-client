package io.reactiverse.pgclient.data;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Polygon data type in Postgres represented by lists of points (the vertexes of the polygon).
 * Polygons are very similar to closed paths, but are stored differently and have their own set of support routines.
 */
@DataObject(generateConverter = true)
public class Polygon {
  private List<Point> points;

  public Polygon() {
    this(new ArrayList<>());
  }

  public Polygon(List<Point> points) {
    this.points = points;
  }


  public Polygon(JsonObject json) {
    PolygonConverter.fromJson(json, this);
  }

  public List<Point> getPoints() {
    return points;
  }

  public void setPoints(List<Point> points) {
    this.points = points;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Polygon polygon = (Polygon) o;

    return points.equals(polygon.points);
  }

  @Override
  public int hashCode() {
    return points.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("Polygon");
    stringBuilder.append("(");
    for (int i = 0; i < points.size(); i++) {
      Point point = points.get(i);
      stringBuilder.append(point.toString());
      if (i != points.size() - 1) {
        // not the last one
        stringBuilder.append(",");
      }
    }
    stringBuilder.append(")");
    return stringBuilder.toString();
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    PolygonConverter.toJson(this, json);
    return json;
  }
}

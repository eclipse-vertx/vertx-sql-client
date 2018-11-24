package io.reactiverse.pgclient.data;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * Finite line segment data type in Postgres represented by pairs of {@link Point}s that are the endpoints of the segment.
 */
@DataObject(generateConverter = true)
public class LineSegment {
  private Point p1, p2;

  public LineSegment() {
    this(new Point(), new Point());
  }

  public LineSegment(Point p1, Point p2) {
    this.p1 = p1;
    this.p2 = p2;
  }

  public LineSegment(JsonObject json) {
    LineSegmentConverter.fromJson(json, this);
  }

  public Point getP1() {
    return p1;
  }

  public void setP1(Point p1) {
    this.p1 = p1;
  }

  public Point getP2() {
    return p2;
  }

  public void setP2(Point p2) {
    this.p2 = p2;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    LineSegment that = (LineSegment) o;

    if (!p1.equals(that.p1)) return false;
    if (!p2.equals(that.p2)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = p1.hashCode();
    result = 31 * result + p2.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "LineSegment[" + p1.toString() + "," + p2.toString() + "]";
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    LineSegmentConverter.toJson(this, json);
    return json;
  }
}

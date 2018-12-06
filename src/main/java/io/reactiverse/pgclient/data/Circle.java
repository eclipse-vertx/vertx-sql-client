package io.reactiverse.pgclient.data;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * Circle data type in Postgres represented by a center {@link Point} and radius.
 */
@DataObject(generateConverter = true)
public class Circle {
  private Point centerPoint;
  private double radius;

  public Circle() {
    this(new Point(), 0.0);
  }

  public Circle(Point centerPoint, double radius) {
    this.centerPoint = centerPoint;
    this.radius = radius;
  }

  public Circle(JsonObject json) {
    CircleConverter.fromJson(json, this);
  }

  public Point getCenterPoint() {
    return centerPoint;
  }

  public void setCenterPoint(Point centerPoint) {
    this.centerPoint = centerPoint;
  }

  public double getRadius() {
    return radius;
  }

  public void setRadius(double radius) {
    this.radius = radius;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Circle that = (Circle) o;

    if (radius != that.radius) return false;
    if (!centerPoint.equals(that.centerPoint)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = centerPoint.hashCode();
    temp = Double.doubleToLongBits(radius);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "Circle<" + centerPoint.toString() + "," + radius + ">";
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    CircleConverter.toJson(this, json);
    return json;
  }
}

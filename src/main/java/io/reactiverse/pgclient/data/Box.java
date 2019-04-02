package io.reactiverse.pgclient.data;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * Rectangular box data type in Postgres represented by pairs of {@link Point}s that are opposite corners of the box.
 */
@DataObject(generateConverter = true)
public class Box {
  private Point upperRightCorner, lowerLeftCorner;

  public Box() {
    this(new Point(), new Point());
  }

  public Box(Point upperRightCorner, Point lowerLeftCorner) {
    this.upperRightCorner = upperRightCorner;
    this.lowerLeftCorner = lowerLeftCorner;
  }

  public Box(JsonObject json) {
    BoxConverter.fromJson(json, this);
  }

  public Point getUpperRightCorner() {
    return upperRightCorner;
  }

  public void setUpperRightCorner(Point upperRightCorner) {
    this.upperRightCorner = upperRightCorner;
  }

  public Point getLowerLeftCorner() {
    return lowerLeftCorner;
  }

  public void setLowerLeftCorner(Point lowerLeftCorner) {
    this.lowerLeftCorner = lowerLeftCorner;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Box box = (Box) o;

    if (!upperRightCorner.equals(box.upperRightCorner)) return false;
    if (!lowerLeftCorner.equals(box.lowerLeftCorner)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = upperRightCorner.hashCode();
    result = 31 * result + lowerLeftCorner.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "Box(" + upperRightCorner.toString() + "," + lowerLeftCorner.toString() + ")";
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    BoxConverter.toJson(this, json);
    return json;
  }
}

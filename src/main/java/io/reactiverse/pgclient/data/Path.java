package io.reactiverse.pgclient.data;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Path data type in Postgres represented by lists of connected points.
 * Paths can be open, where the first and last points in the list are considered not connected,
 * or closed, where the first and last points are considered connected.
 */
@DataObject(generateConverter = true)
public class Path {
  private boolean isOpen;
  private List<Point> points;

  public Path() {
    this(false, new ArrayList<>());
  }

  public Path(boolean isOpen, List<Point> points) {
    this.isOpen = isOpen;
    this.points = points;
  }


  public Path(JsonObject json) {
    PathConverter.fromJson(json, this);
  }

  public boolean isOpen() {
    return isOpen;
  }

  public void setOpen(boolean open) {
    isOpen = open;
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

    Path path = (Path) o;

    if (isOpen != path.isOpen) return false;
    return points.equals(path.points);
  }

  @Override
  public int hashCode() {
    int result = (isOpen ? 1 : 0);
    result = 31 * result + points.hashCode();
    return result;
  }

  @Override
  public String toString() {
    String left;
    String right;
    if (isOpen) {
      left = "[";
      right = "]";
    } else {
      left = "(";
      right = ")";
    }
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("Path");
    stringBuilder.append(left);
    for (int i = 0; i < points.size(); i++) {
      Point point = points.get(i);
      stringBuilder.append(point.toString());
      if (i != points.size() - 1) {
        // not the last one
        stringBuilder.append(",");
      }
    }
    stringBuilder.append(right);
    return stringBuilder.toString();
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    PathConverter.toJson(this, json);
    return json;
  }
}

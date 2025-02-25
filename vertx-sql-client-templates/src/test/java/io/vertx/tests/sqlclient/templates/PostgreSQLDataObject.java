package io.vertx.tests.sqlclient.templates;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.data.Box;
import io.vertx.pgclient.data.Circle;
import io.vertx.pgclient.data.Interval;
import io.vertx.pgclient.data.Line;
import io.vertx.pgclient.data.LineSegment;
import io.vertx.pgclient.data.Path;
import io.vertx.pgclient.data.Point;
import io.vertx.pgclient.data.Polygon;
import io.vertx.sqlclient.templates.annotations.ParametersMapped;
import io.vertx.sqlclient.templates.annotations.RowMapped;

@DataObject
@RowMapped
@ParametersMapped
public class PostgreSQLDataObject {

  private Box box;
  private Circle circle;
  private Interval interval;
  private Line line;
  private LineSegment lineSegment;
  private Path path;
  private Point point;
  private Polygon polygon;

  public PostgreSQLDataObject() {
  }

  public PostgreSQLDataObject(JsonObject json) {
  }

  public Box getBox() {
    return box;
  }

  public void setBox(Box box) {
    this.box = box;
  }

  public Circle getCircle() {
    return circle;
  }

  public void setCircle(Circle circle) {
    this.circle = circle;
  }

  public Interval getInterval() {
    return interval;
  }

  public void setInterval(Interval interval) {
    this.interval = interval;
  }

  public Line getLine() {
    return line;
  }

  public void setLine(Line line) {
    this.line = line;
  }

  public LineSegment getLineSegment() {
    return lineSegment;
  }

  public void setLineSegment(LineSegment lineSegment) {
    this.lineSegment = lineSegment;
  }

  public Path getPath() {
    return path;
  }

  public void setPath(Path path) {
    this.path = path;
  }

  public Point getPoint() {
    return point;
  }

  public void setPoint(Point point) {
    this.point = point;
  }

  public Polygon getPolygon() {
    return polygon;
  }

  public void setPolygon(Polygon polygon) {
    this.polygon = polygon;
  }
}

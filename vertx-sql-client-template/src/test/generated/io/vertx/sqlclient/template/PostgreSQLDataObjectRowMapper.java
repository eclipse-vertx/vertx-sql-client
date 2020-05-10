package io.vertx.sqlclient.template;

/**
 * Mapper for {@link PostgreSQLDataObject}.
 * NOTE: This class has been automatically generated from the {@link PostgreSQLDataObject} original class using Vert.x codegen.
 */
public class PostgreSQLDataObjectRowMapper implements java.util.function.Function<io.vertx.sqlclient.Row, PostgreSQLDataObject> {

  public static final java.util.function.Function<io.vertx.sqlclient.Row, PostgreSQLDataObject> INSTANCE = new PostgreSQLDataObjectRowMapper();

  public static final java.util.stream.Collector<io.vertx.sqlclient.Row, ?, java.util.List<PostgreSQLDataObject>> COLLECTOR = java.util.stream.Collectors.mapping(INSTANCE, java.util.stream.Collectors.toList());

  public PostgreSQLDataObject apply(io.vertx.sqlclient.Row row) {
    PostgreSQLDataObject obj = new PostgreSQLDataObject();
    Object val;
    val = row.get(io.vertx.pgclient.data.Box.class, "box");
    if (val != null) {
      obj.setBox((io.vertx.pgclient.data.Box)val);
    }
    val = row.get(io.vertx.pgclient.data.Circle.class, "circle");
    if (val != null) {
      obj.setCircle((io.vertx.pgclient.data.Circle)val);
    }
    val = row.get(io.vertx.pgclient.data.Interval.class, "interval");
    if (val != null) {
      obj.setInterval((io.vertx.pgclient.data.Interval)val);
    }
    val = row.get(io.vertx.pgclient.data.Line.class, "line");
    if (val != null) {
      obj.setLine((io.vertx.pgclient.data.Line)val);
    }
    val = row.get(io.vertx.pgclient.data.LineSegment.class, "lineSegment");
    if (val != null) {
      obj.setLineSegment((io.vertx.pgclient.data.LineSegment)val);
    }
    val = row.get(io.vertx.pgclient.data.Path.class, "path");
    if (val != null) {
      obj.setPath((io.vertx.pgclient.data.Path)val);
    }
    val = row.get(io.vertx.pgclient.data.Point.class, "point");
    if (val != null) {
      obj.setPoint((io.vertx.pgclient.data.Point)val);
    }
    val = row.get(io.vertx.pgclient.data.Polygon.class, "polygon");
    if (val != null) {
      obj.setPolygon((io.vertx.pgclient.data.Polygon)val);
    }
    return obj;
  }
}

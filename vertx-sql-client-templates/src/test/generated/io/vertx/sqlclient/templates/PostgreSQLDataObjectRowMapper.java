package io.vertx.sqlclient.templates;

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
    int idx;
    if ((idx = row.getColumnIndex("box")) != -1 && (val = row.get(io.vertx.pgclient.data.Box.class, idx)) != null) {
      obj.setBox((io.vertx.pgclient.data.Box)val);
    }
    if ((idx = row.getColumnIndex("circle")) != -1 && (val = row.get(io.vertx.pgclient.data.Circle.class, idx)) != null) {
      obj.setCircle((io.vertx.pgclient.data.Circle)val);
    }
    if ((idx = row.getColumnIndex("interval")) != -1 && (val = row.get(io.vertx.pgclient.data.Interval.class, idx)) != null) {
      obj.setInterval((io.vertx.pgclient.data.Interval)val);
    }
    if ((idx = row.getColumnIndex("line")) != -1 && (val = row.get(io.vertx.pgclient.data.Line.class, idx)) != null) {
      obj.setLine((io.vertx.pgclient.data.Line)val);
    }
    if ((idx = row.getColumnIndex("lineSegment")) != -1 && (val = row.get(io.vertx.pgclient.data.LineSegment.class, idx)) != null) {
      obj.setLineSegment((io.vertx.pgclient.data.LineSegment)val);
    }
    if ((idx = row.getColumnIndex("path")) != -1 && (val = row.get(io.vertx.pgclient.data.Path.class, idx)) != null) {
      obj.setPath((io.vertx.pgclient.data.Path)val);
    }
    if ((idx = row.getColumnIndex("point")) != -1 && (val = row.get(io.vertx.pgclient.data.Point.class, idx)) != null) {
      obj.setPoint((io.vertx.pgclient.data.Point)val);
    }
    if ((idx = row.getColumnIndex("polygon")) != -1 && (val = row.get(io.vertx.pgclient.data.Polygon.class, idx)) != null) {
      obj.setPolygon((io.vertx.pgclient.data.Polygon)val);
    }
    return obj;
  }
}

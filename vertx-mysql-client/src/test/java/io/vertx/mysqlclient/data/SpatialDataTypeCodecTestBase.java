package io.vertx.mysqlclient.data;

import io.vertx.ext.unit.TestContext;
import io.vertx.mysqlclient.data.spatial.*;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import org.junit.Test;

import java.util.List;
import java.util.function.Consumer;

public abstract class SpatialDataTypeCodecTestBase extends MySQLDataTypeTestBase {
  @Test
  public void testDecodePoint(TestContext ctx) {
    testDecodeGeometry(ctx, "SELECT ST_GeometryFromText('POINT(1.5 5.1)', 0) AS test_geometry;", result -> {
      Row row = result.iterator().next();
      Point point = row.get(Point.class, 0);
      ctx.assertEquals(0L, point.getSRID());
      ctx.assertEquals(1.5d, point.getX());
      ctx.assertEquals(5.1d, point.getY());
    });
  }

  @Test
  public void testDecodeLineString(TestContext ctx) {
    testDecodeGeometry(ctx, "SELECT ST_GeometryFromText('LINESTRING(0 0,1 1,2 2)', 0) AS test_geometry;", result -> {
      Row row = result.iterator().next();
      LineString lineString = row.get(LineString.class, 0);
      ctx.assertEquals(0L, lineString.getSRID());
      List<Point> points = lineString.getPoints();
      ctx.assertEquals(3, points.size());
      ctx.assertEquals(0d, points.get(0).getX());
      ctx.assertEquals(0d, points.get(0).getY());
      ctx.assertEquals(1d, points.get(1).getX());
      ctx.assertEquals(1d, points.get(1).getY());
      ctx.assertEquals(2d, points.get(2).getX());
      ctx.assertEquals(2d, points.get(2).getY());
    });
  }

  @Test
  public void testDecodePolygon(TestContext ctx) {
    testDecodeGeometry(ctx, "SELECT ST_GeometryFromText('POLYGON((0 0,10 0,10 10,0 10,0 0),(5 5,7 5,7 7,5 7,5 5))', 0) AS test_geometry;", result -> {
      Row row = result.iterator().next();
      Polygon polygon = row.get(Polygon.class, 0);
      ctx.assertEquals(0L, polygon.getSRID());
      List<LineString> lineStrings = polygon.getLineStrings();
      ctx.assertEquals(2, lineStrings.size());
      List<Point> pointsOfFirstLineString = lineStrings.get(0).getPoints();
      ctx.assertEquals(5, pointsOfFirstLineString.size());
      ctx.assertEquals(0d, pointsOfFirstLineString.get(0).getX());
      ctx.assertEquals(0d, pointsOfFirstLineString.get(0).getY());
      ctx.assertEquals(10d, pointsOfFirstLineString.get(1).getX());
      ctx.assertEquals(0d, pointsOfFirstLineString.get(1).getY());
      ctx.assertEquals(10d, pointsOfFirstLineString.get(2).getX());
      ctx.assertEquals(10d, pointsOfFirstLineString.get(2).getY());
      ctx.assertEquals(0d, pointsOfFirstLineString.get(3).getX());
      ctx.assertEquals(10d, pointsOfFirstLineString.get(3).getY());
      ctx.assertEquals(0d, pointsOfFirstLineString.get(4).getX());
      ctx.assertEquals(0d, pointsOfFirstLineString.get(4).getY());

      List<Point> pointsOfSecondLineString = lineStrings.get(1).getPoints();
      ctx.assertEquals(5, pointsOfSecondLineString.size());
      ctx.assertEquals(5d, pointsOfSecondLineString.get(0).getX());
      ctx.assertEquals(5d, pointsOfSecondLineString.get(0).getY());
      ctx.assertEquals(7d, pointsOfSecondLineString.get(1).getX());
      ctx.assertEquals(5d, pointsOfSecondLineString.get(1).getY());
      ctx.assertEquals(7d, pointsOfSecondLineString.get(2).getX());
      ctx.assertEquals(7d, pointsOfSecondLineString.get(2).getY());
      ctx.assertEquals(5d, pointsOfSecondLineString.get(3).getX());
      ctx.assertEquals(7d, pointsOfSecondLineString.get(3).getY());
      ctx.assertEquals(5d, pointsOfSecondLineString.get(4).getX());
      ctx.assertEquals(5d, pointsOfSecondLineString.get(4).getY());
    });
  }

  @Test
  public void testDecodeMultiPoint(TestContext ctx) {
    testDecodeGeometry(ctx, "SELECT ST_GeometryFromText('MULTIPOINT(0 0,1 1,2 2)', 0) AS test_geometry;", result -> {
      Row row = result.iterator().next();
      MultiPoint multiPoint = row.get(MultiPoint.class, 0);
      ctx.assertEquals(0L, multiPoint.getSRID());
      List<Point> points = multiPoint.getPoints();
      ctx.assertEquals(3, points.size());
      ctx.assertEquals(0d, points.get(0).getX());
      ctx.assertEquals(0d, points.get(0).getY());
      ctx.assertEquals(1d, points.get(1).getX());
      ctx.assertEquals(1d, points.get(1).getY());
      ctx.assertEquals(2d, points.get(2).getX());
      ctx.assertEquals(2d, points.get(2).getY());
    });
  }

  @Test
  public void testDecodeMultiLineString(TestContext ctx) {
    testDecodeGeometry(ctx, "SELECT ST_GeometryFromText('MULTILINESTRING((1 1,2 2,3 3),(4 4,5 5))', 0) AS test_geometry;", result -> {
      Row row = result.iterator().next();
      MultiLineString multiLineString = row.get(MultiLineString.class, 0);
      ctx.assertEquals(0L, multiLineString.getSRID());
      List<LineString> lineStrings = multiLineString.getLineStrings();
      ctx.assertEquals(2, lineStrings.size());
      List<Point> pointsOfFirstLineString = lineStrings.get(0).getPoints();
      ctx.assertEquals(3, pointsOfFirstLineString.size());
      ctx.assertEquals(1d, pointsOfFirstLineString.get(0).getX());
      ctx.assertEquals(1d, pointsOfFirstLineString.get(0).getY());
      ctx.assertEquals(2d, pointsOfFirstLineString.get(1).getX());
      ctx.assertEquals(2d, pointsOfFirstLineString.get(1).getY());
      ctx.assertEquals(3d, pointsOfFirstLineString.get(2).getX());
      ctx.assertEquals(3d, pointsOfFirstLineString.get(2).getY());

      List<Point> pointsOfSecondLineString = lineStrings.get(1).getPoints();
      ctx.assertEquals(2, pointsOfSecondLineString.size());
      ctx.assertEquals(4d, pointsOfSecondLineString.get(0).getX());
      ctx.assertEquals(4d, pointsOfSecondLineString.get(0).getY());
      ctx.assertEquals(5d, pointsOfSecondLineString.get(1).getX());
      ctx.assertEquals(5d, pointsOfSecondLineString.get(1).getY());
    });
  }

  @Test
  public void testDecodeMultiPolygon(TestContext ctx) {
    testDecodeGeometry(ctx, "SELECT ST_GeometryFromText('MULTIPOLYGON(((0 0,0 3,3 3,3 0,0 0),(1 1,1 2,2 2,2 1,1 1)))', 0) AS test_geometry;", result -> {
      Row row = result.iterator().next();
      MultiPolygon multiPolygon = row.get(MultiPolygon.class, 0);
      ctx.assertEquals(0L, multiPolygon.getSRID());
      List<Polygon> polygons = multiPolygon.getPolygons();
      ctx.assertEquals(1, polygons.size());
      Polygon polygon = polygons.get(0);
      List<LineString> lineStrings = polygon.getLineStrings();
      ctx.assertEquals(2, lineStrings.size());
      List<Point> pointsOfFirstLineString = lineStrings.get(0).getPoints();
      ctx.assertEquals(5, pointsOfFirstLineString.size());
      ctx.assertEquals(0d, pointsOfFirstLineString.get(0).getX());
      ctx.assertEquals(0d, pointsOfFirstLineString.get(0).getY());
      ctx.assertEquals(0d, pointsOfFirstLineString.get(1).getX());
      ctx.assertEquals(3d, pointsOfFirstLineString.get(1).getY());
      ctx.assertEquals(3d, pointsOfFirstLineString.get(2).getX());
      ctx.assertEquals(3d, pointsOfFirstLineString.get(2).getY());
      ctx.assertEquals(3d, pointsOfFirstLineString.get(3).getX());
      ctx.assertEquals(0d, pointsOfFirstLineString.get(3).getY());
      ctx.assertEquals(0d, pointsOfFirstLineString.get(4).getX());
      ctx.assertEquals(0d, pointsOfFirstLineString.get(4).getY());

      List<Point> pointsOfSecondLineString = lineStrings.get(1).getPoints();
      ctx.assertEquals(5, pointsOfSecondLineString.size());
      ctx.assertEquals(1d, pointsOfSecondLineString.get(0).getX());
      ctx.assertEquals(1d, pointsOfSecondLineString.get(0).getY());
      ctx.assertEquals(1d, pointsOfSecondLineString.get(1).getX());
      ctx.assertEquals(2d, pointsOfSecondLineString.get(1).getY());
      ctx.assertEquals(2d, pointsOfSecondLineString.get(2).getX());
      ctx.assertEquals(2d, pointsOfSecondLineString.get(2).getY());
      ctx.assertEquals(2d, pointsOfSecondLineString.get(3).getX());
      ctx.assertEquals(1d, pointsOfSecondLineString.get(3).getY());
      ctx.assertEquals(1d, pointsOfSecondLineString.get(4).getX());
      ctx.assertEquals(1d, pointsOfSecondLineString.get(4).getY());
    });
  }

  @Test
  public void testDecodeGeometryCollection(TestContext ctx) {
    testDecodeGeometry(ctx, "SELECT ST_GeometryFromText('GEOMETRYCOLLECTION(Point(1 1),LineString(2 2, 3 3))', 0) AS test_geometry;", result -> {
      Row row = result.iterator().next();
      GeometryCollection geometryCollection = row.get(GeometryCollection.class, 0);
      ctx.assertEquals(0L, geometryCollection.getSRID());
      List<Geometry> geometries = geometryCollection.getGeometries();
      ctx.assertEquals(2, geometries.size());

      ctx.assertTrue(geometries.get(0) instanceof Point);
      Point firstGeometry = (Point) geometries.get(0);
      ctx.assertEquals(1d, firstGeometry.getX());
      ctx.assertEquals(1d, firstGeometry.getY());

      ctx.assertTrue(geometries.get(1) instanceof LineString);
      LineString secondGeometry = (LineString) geometries.get(1);
      List<Point> pointsOfSecondGeometry = secondGeometry.getPoints();
      ctx.assertEquals(2, pointsOfSecondGeometry.size());
      ctx.assertEquals(2d, pointsOfSecondGeometry.get(0).getX());
      ctx.assertEquals(2d, pointsOfSecondGeometry.get(0).getY());
      ctx.assertEquals(3d, pointsOfSecondGeometry.get(1).getY());
      ctx.assertEquals(3d, pointsOfSecondGeometry.get(1).getY());
    });
  }

  protected abstract void testDecodeGeometry(TestContext ctx, String sql, Consumer<RowSet<Row>> checker);
}

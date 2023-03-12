/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mysqlclient.data;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mysqlclient.MySQLConnection;
import io.vertx.mysqlclient.data.spatial.*;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@RunWith(VertxUnitRunner.class)
public class SpatialBinaryCodecTest extends SpatialDataTypeCodecTestBase {

  @Override
  protected void testDecodeGeometry(TestContext ctx, String sql, Consumer<RowSet<Row>> checker) {
    testBinaryDecode(ctx, sql, checker);
  }

  @Test
  public void testEncodePoint(TestContext ctx) {
    Point point = new Point(0, 1.5d, 5.1d);

    testBinaryEncodeGeometry(ctx, point, result -> {
      Row row = result.iterator().next();
      String text = row.getString(0);
      ctx.assertEquals("POINT(1.5 5.1)", text);
    });
  }

  @Test
  public void testEncodeLineString(TestContext ctx) {
    List<Point> points = new ArrayList<>();
    points.add(new Point(0, 1.1, 1.1));
    points.add(new Point(0, 2.2, 2.2));
    LineString lineString = new LineString(0, points);

    testBinaryEncodeGeometry(ctx, lineString, result -> {
      Row row = result.iterator().next();
      String text = row.getString(0);
      ctx.assertEquals("LINESTRING(1.1 1.1,2.2 2.2)", text);
    });
  }

  @Test
  public void testEncodePolygon(TestContext ctx) {
    List<Point> pointsOfFirstLineString = new ArrayList<>();
    pointsOfFirstLineString.add(new Point(0, 0, 0));
    pointsOfFirstLineString.add(new Point(0, 10, 0));
    pointsOfFirstLineString.add(new Point(0, 10, 10));
    pointsOfFirstLineString.add(new Point(0, 0, 10));
    pointsOfFirstLineString.add(new Point(0, 0, 0));
    LineString firstLineString = new LineString(0, pointsOfFirstLineString);

    List<Point> pointsOfSecondLineString = new ArrayList<>();
    pointsOfSecondLineString.add(new Point(0, 5, 5));
    pointsOfSecondLineString.add(new Point(0, 7, 5));
    pointsOfSecondLineString.add(new Point(0, 7, 7));
    pointsOfSecondLineString.add(new Point(0, 5, 7));
    pointsOfSecondLineString.add(new Point(0, 5, 5));
    LineString secondLineString = new LineString(0, pointsOfSecondLineString);

    List<LineString> lineStrings = new ArrayList<>();
    lineStrings.add(firstLineString);
    lineStrings.add(secondLineString);
    Polygon polygon = new Polygon(0, lineStrings);

    testBinaryEncodeGeometry(ctx, polygon, result -> {
      Row row = result.iterator().next();
      String text = row.getString(0);
      ctx.assertEquals("POLYGON((0 0,10 0,10 10,0 10,0 0),(5 5,7 5,7 7,5 7,5 5))", text);
    });
  }

  @Test
  public void testEncodeMultiPoint(TestContext ctx) {
    List<Point> points = new ArrayList<>();
    points.add(new Point(0, 0, 0));
    points.add(new Point(0, 1, 1));
    points.add(new Point(0, 2, 2));
    MultiPoint multiPoint = new MultiPoint(0, points);

    testBinaryEncodeGeometry(ctx, multiPoint, result -> {
      Row row = result.iterator().next();
      String text = row.getString(0);
      // a workaround for MySQL 5.6
      boolean expected1 = "MULTIPOINT(0 0,1 1,2 2)".equals(text);
      boolean expected2 = "MULTIPOINT((0 0),(1 1),(2 2))".equals(text);
      ctx.assertTrue(expected1 || expected2);
    });
  }

  @Test
  public void testEncodeMultiLineString(TestContext ctx) {
    List<Point> pointsOfFirstLineString = new ArrayList<>();
    pointsOfFirstLineString.add(new Point(0, 1, 1));
    pointsOfFirstLineString.add(new Point(0, 2, 2));
    pointsOfFirstLineString.add(new Point(0, 3, 3));
    LineString firstLineString = new LineString(0, pointsOfFirstLineString);

    List<Point> pointsOfSecondLineString = new ArrayList<>();
    pointsOfSecondLineString.add(new Point(0, 4, 4));
    pointsOfSecondLineString.add(new Point(0, 5, 5));
    LineString secondLineString = new LineString(0, pointsOfSecondLineString);

    List<LineString> lineStrings = new ArrayList<>();
    lineStrings.add(firstLineString);
    lineStrings.add(secondLineString);
    MultiLineString multiLineString = new MultiLineString(0, lineStrings);

    testBinaryEncodeGeometry(ctx, multiLineString, result -> {
      Row row = result.iterator().next();
      String text = row.getString(0);
      ctx.assertEquals("MULTILINESTRING((1 1,2 2,3 3),(4 4,5 5))", text);
    });
  }

  @Test
  public void testEncodeMultiPolygon(TestContext ctx) {
    List<Point> pointsOfFirstLineStringOfFirstPolygon = new ArrayList<>();
    pointsOfFirstLineStringOfFirstPolygon.add(new Point(0, 0, 0));
    pointsOfFirstLineStringOfFirstPolygon.add(new Point(0, 0, 3));
    pointsOfFirstLineStringOfFirstPolygon.add(new Point(0, 3, 3));
    pointsOfFirstLineStringOfFirstPolygon.add(new Point(0, 3, 0));
    pointsOfFirstLineStringOfFirstPolygon.add(new Point(0, 0, 0));
    LineString firstLineString = new LineString(0, pointsOfFirstLineStringOfFirstPolygon);

    List<Point> pointsOfSecondLineStringOfFirstPolygon = new ArrayList<>();
    pointsOfSecondLineStringOfFirstPolygon.add(new Point(0, 1, 1));
    pointsOfSecondLineStringOfFirstPolygon.add(new Point(0, 1, 2));
    pointsOfSecondLineStringOfFirstPolygon.add(new Point(0, 2, 2));
    pointsOfSecondLineStringOfFirstPolygon.add(new Point(0, 2, 1));
    pointsOfSecondLineStringOfFirstPolygon.add(new Point(0, 1, 1));
    LineString secondLineString = new LineString(0, pointsOfSecondLineStringOfFirstPolygon);

    List<LineString> lineStrings = new ArrayList<>();
    lineStrings.add(firstLineString);
    lineStrings.add(secondLineString);
    Polygon polygon = new Polygon(0, lineStrings);
    List<Polygon> polygons = new ArrayList<>();
    polygons.add(polygon);

    MultiPolygon multiPolygon = new MultiPolygon(0, polygons);

    testBinaryEncodeGeometry(ctx, multiPolygon, result -> {
      Row row = result.iterator().next();
      String text = row.getString(0);
      ctx.assertEquals("MULTIPOLYGON(((0 0,0 3,3 3,3 0,0 0),(1 1,1 2,2 2,2 1,1 1)))", text);
    });
  }

  @Test
  public void testEncodeGeometryCollection(TestContext ctx) {
    Point point = new Point(0, 1, 1);
    Point firstPointOfLineString = new Point(0, 2, 2);
    Point secondPointOfLineString = new Point(0, 3, 3);
    List<Point> pointsOfLineString = new ArrayList<>();
    pointsOfLineString.add(firstPointOfLineString);
    pointsOfLineString.add(secondPointOfLineString);
    LineString lineString = new LineString(0, pointsOfLineString);

    List<Geometry> geometries = new ArrayList<>();
    geometries.add(point);
    geometries.add(lineString);
    GeometryCollection geometryCollection = new GeometryCollection(0, geometries);

    testBinaryEncodeGeometry(ctx, geometryCollection, result -> {
      Row row = result.iterator().next();
      String text = row.getString(0);
      ctx.assertEquals("GEOMETRYCOLLECTION(POINT(1 1),LINESTRING(2 2,3 3))", text);
    });
  }

  private void testBinaryEncodeGeometry(TestContext ctx, Object param, Consumer<RowSet<Row>> checker) {
    MySQLConnection.connect(vertx, options).onComplete( ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT ST_AsText(ST_GeomFromWKB(?)) AS test_geometry;").execute(Tuple.of(param), ctx.asyncAssertSuccess(res -> {
        checker.accept(res);
        conn.close();
      }));
    }));
  }
}

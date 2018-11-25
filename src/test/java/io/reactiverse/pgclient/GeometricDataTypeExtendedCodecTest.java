package io.reactiverse.pgclient;

import io.reactiverse.pgclient.data.Box;
import io.reactiverse.pgclient.data.Circle;
import io.reactiverse.pgclient.data.Line;
import io.reactiverse.pgclient.data.LineSegment;
import io.reactiverse.pgclient.data.Path;
import io.reactiverse.pgclient.data.Point;
import io.reactiverse.pgclient.data.Polygon;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.Arrays;

public class GeometricDataTypeExtendedCodecTest extends DataTypeTestBase {
  @Override
  protected PgConnectOptions options() {
    return new PgConnectOptions(options).setCachePreparedStatements(false);
  }

  @Test
  public void testDecodeGeometric(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Point\", \"Line\", \"Lseg\", \"Box\", \"ClosedPath\", \"OpenPath\", \"Polygon\", \"Circle\" FROM \"GeometricDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          Point point = new Point(1.0, 2.0);
          Line line = new Line(1.0, 2.0, 3.0);
          LineSegment lineSegment = new LineSegment(new Point(1.0, 1.0), new Point(2.0, 2.0));
          Box box = new Box(new Point(2.0, 2.0), new Point(1.0, 1.0));
          Path closedPath = new Path(false, Arrays.asList(new Point(1.0, 1.0), new Point(2.0, 1.0), new Point(2.0, 2.0), new Point(2.0, 1.0)));
          Path openPath = new Path(true, Arrays.asList(new Point(1.0, 1.0), new Point(2.0, 1.0), new Point(2.0, 2.0), new Point(2.0, 1.0)));
          Polygon polygon = new Polygon(Arrays.asList(new Point(1.0, 1.0), new Point(2.0, 2.0), new Point(3.0, 1.0)));
          Circle circle = new Circle(new Point(1.0, 1.0), 1.0);
          int id = 1;
          p.execute(Tuple.tuple().addInteger(id), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            ColumnChecker.checkColumn(0, "Point")
              .returns(Tuple::getValue, Row::getValue, point)
              .returns(Tuple::getPoint, Row::getPoint, point)
              .forRow(row);
            ColumnChecker.checkColumn(1, "Line")
              .returns(Tuple::getValue, Row::getValue, line)
              .returns(Tuple::getLine, Row::getLine, line)
              .forRow(row);
            ColumnChecker.checkColumn(2, "Lseg")
              .returns(Tuple::getValue, Row::getValue, lineSegment)
              .returns(Tuple::getLineSegment, Row::getLineSegment, lineSegment)
              .forRow(row);
            ColumnChecker.checkColumn(3, "Box")
              .returns(Tuple::getValue, Row::getValue, box)
              .returns(Tuple::getBox, Row::getBox, box)
              .forRow(row);
            ColumnChecker.checkColumn(4, "ClosedPath")
              .returns(Tuple::getValue, Row::getValue, closedPath)
              .returns(Tuple::getPath, Row::getPath, closedPath)
              .forRow(row);
            ColumnChecker.checkColumn(5, "OpenPath")
              .returns(Tuple::getValue, Row::getValue, openPath)
              .returns(Tuple::getPath, Row::getPath, openPath)
              .forRow(row);
            ColumnChecker.checkColumn(6, "Polygon")
              .returns(Tuple::getValue, Row::getValue, polygon)
              .returns(Tuple::getPolygon, Row::getPolygon, polygon)
              .forRow(row);
            ColumnChecker.checkColumn(7, "Circle")
              .returns(Tuple::getValue, Row::getValue, circle)
              .returns(Tuple::getCircle, Row::getCircle, circle)
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testDecodeGeometricArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"Point\", \"Line\", \"Lseg\", \"Box\", \"ClosedPath\", \"OpenPath\", \"Polygon\", \"Circle\" FROM \"ArrayDataType\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          Point[] points = {new Point(1.0, 1.0), new Point(2.0, 2.0)};
          Line[] lines = {new Line(1.0, 2.0, 3.0), new Line(2.0, 3.0, 4.0)};
          LineSegment[] lineSegments = {new LineSegment(new Point(1.0, 1.0), new Point(2.0, 2.0)), new LineSegment(new Point(2.0, 2.0), new Point(3.0, 3.0))};
          Box[] boxes = {new Box(new Point(2.0, 2.0), new Point(1.0, 1.0)), new Box(new Point(3.0, 3.0), new Point(2.0, 2.0))};
          Path[] closedPaths = {new Path(false, Arrays.asList(new Point(1.0, 1.0), new Point(2.0, 1.0), new Point(2.0, 2.0), new Point(2.0, 1.0))),
            new Path(false, Arrays.asList(new Point(2.0, 2.0), new Point(3.0, 2.0), new Point(3.0, 3.0), new Point(3.0, 2.0)))};
          Path[] openPaths = {new Path(true, Arrays.asList(new Point(1.0, 1.0), new Point(2.0, 1.0), new Point(2.0, 2.0), new Point(2.0, 1.0))),
            new Path(true, Arrays.asList(new Point(2.0, 2.0), new Point(3.0, 2.0), new Point(3.0, 3.0), new Point(3.0, 2.0)))};
          Polygon[] polygons = {new Polygon(Arrays.asList(new Point(1.0, 1.0), new Point(2.0, 2.0), new Point(3.0, 1.0))),
            new Polygon(Arrays.asList(new Point(0.0, 0.0), new Point(0.0, 1.0), new Point(1.0, 2.0), new Point(2.0, 1.0), new Point(2.0, 0.0)))};
          Circle[] circles = {new Circle(new Point(1.0, 1.0), 1.0), new Circle(new Point(0.0, 0.0), 2.0)};
          int id = 1;
          p.execute(Tuple.tuple().addInteger(id), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            ColumnChecker.checkColumn(0, "Point")
              .returns(Tuple::getValue, Row::getValue, points)
              .returns(Tuple::getPointArray, Row::getPointArray, points)
              .forRow(row);
            ColumnChecker.checkColumn(1, "Line")
              .returns(Tuple::getValue, Row::getValue, lines)
              .returns(Tuple::getLineArray, Row::getLineArray, lines)
              .forRow(row);
            ColumnChecker.checkColumn(2, "Lseg")
              .returns(Tuple::getValue, Row::getValue, lineSegments)
              .returns(Tuple::getLineSegmentArray, Row::getLineSegmentArray, lineSegments)
              .forRow(row);
            ColumnChecker.checkColumn(3, "Box")
              .returns(Tuple::getValue, Row::getValue, boxes)
              .returns(Tuple::getBoxArray, Row::getBoxArray, boxes)
              .forRow(row);
            ColumnChecker.checkColumn(4, "ClosedPath")
              .returns(Tuple::getValue, Row::getValue, closedPaths)
              .returns(Tuple::getPathArray, Row::getPathArray, closedPaths)
              .forRow(row);
            ColumnChecker.checkColumn(5, "OpenPath")
              .returns(Tuple::getValue, Row::getValue, openPaths)
              .returns(Tuple::getPathArray, Row::getPathArray, openPaths)
              .forRow(row);
            ColumnChecker.checkColumn(6, "Polygon")
              .returns(Tuple::getValue, Row::getValue, polygons)
              .returns(Tuple::getPolygonArray, Row::getPolygonArray, polygons)
              .forRow(row);
            ColumnChecker.checkColumn(7, "Circle")
              .returns(Tuple::getValue, Row::getValue, circles)
              .returns(Tuple::getCircleArray, Row::getCircleArray, circles)
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeGeometric(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"" + "GeometricDataType" + "\" SET " +
          "\"Point\" = $1, " +
          "\"Line\" = $2, " +
          "\"Lseg\" = $3, " +
          "\"Box\" = $4, " +
          "\"ClosedPath\" = $5, " +
          "\"OpenPath\" = $6, " +
          "\"Polygon\" = $7, " +
          "\"Circle\" = $8 " +
          "WHERE \"id\" = $9 RETURNING \"Point\", \"Line\", \"Lseg\", \"Box\", \"ClosedPath\", \"OpenPath\", \"Polygon\", \"Circle\"",
        ctx.asyncAssertSuccess(p -> {
          Point point = new Point(2.0, 3.0);
          Line line = new Line(2.0, 3.0, 4.0);
          LineSegment lineSegment = new LineSegment(new Point(2.0, 2.0), new Point(3.0, 3.0));
          Box box = new Box(new Point(3.0, 3.0), new Point(2.0, 2.0));
          Path OpenPath = new Path(true, Arrays.asList(new Point(1.0, 1.0), new Point(2.0, 1.0), new Point(2.0, 2.0), new Point(2.0, 1.0)));
          Path closedPath = new Path(false, Arrays.asList(new Point(1.0, 1.0), new Point(2.0, 1.0), new Point(2.0, 2.0), new Point(2.0, 1.0)));
          Polygon polygon = new Polygon(Arrays.asList(new Point(2.0, 2.0), new Point(3.0, 3.0), new Point(4.0, 2.0)));
          Circle circle = new Circle(new Point(1.0, 1.0), 3.0);
          int id = 2;
          p.execute(Tuple.tuple()
            .addPoint(point)
            .addLine(line)
            .addLineSegment(lineSegment)
            .addBox(box)
            .addPath(OpenPath)
            .addPath(closedPath)
            .addPolygon(polygon)
            .addCircle(circle)
            .addInteger(id), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            ColumnChecker.checkColumn(0, "Point")
              .returns(Tuple::getValue, Row::getValue, point)
              .returns(Tuple::getPoint, Row::getPoint, point)
              .forRow(row);
            ColumnChecker.checkColumn(1, "Line")
              .returns(Tuple::getValue, Row::getValue, line)
              .returns(Tuple::getLine, Row::getLine, line)
              .forRow(row);
            ColumnChecker.checkColumn(2, "Lseg")
              .returns(Tuple::getValue, Row::getValue, lineSegment)
              .returns(Tuple::getLineSegment, Row::getLineSegment, lineSegment)
              .forRow(row);
            ColumnChecker.checkColumn(3, "Box")
              .returns(Tuple::getValue, Row::getValue, box)
              .returns(Tuple::getBox, Row::getBox, box)
              .forRow(row);
            ColumnChecker.checkColumn(4, "ClosedPath")
              .returns(Tuple::getValue, Row::getValue, OpenPath)
              .returns(Tuple::getPath, Row::getPath, OpenPath)
              .forRow(row);
            ColumnChecker.checkColumn(5, "OpenPath")
              .returns(Tuple::getValue, Row::getValue, closedPath)
              .returns(Tuple::getPath, Row::getPath, closedPath)
              .forRow(row);
            ColumnChecker.checkColumn(6, "Polygon")
              .returns(Tuple::getValue, Row::getValue, polygon)
              .returns(Tuple::getPolygon, Row::getPolygon, polygon)
              .forRow(row);
            ColumnChecker.checkColumn(7, "Circle")
              .returns(Tuple::getValue, Row::getValue, circle)
              .returns(Tuple::getCircle, Row::getCircle, circle)
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeGeometricArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"" + "ArrayDataType" + "\" SET " +
          "\"Point\" = $1, " +
          "\"Line\" = $2, " +
          "\"Lseg\" = $3, " +
          "\"Box\" = $4, " +
          "\"ClosedPath\" = $5, " +
          "\"OpenPath\" = $6, " +
          "\"Polygon\" = $7, " +
          "\"Circle\" = $8 " +
          "WHERE \"id\" = $9 RETURNING \"Point\", \"Line\", \"Lseg\", \"Box\", \"ClosedPath\", \"OpenPath\", \"Polygon\", \"Circle\"",
        ctx.asyncAssertSuccess(p -> {
          Point[] points = {new Point(2.0, 2.0), new Point(1.0, 1.0)};
          Line[] lines = {new Line(3.0, 2.0, 1.0), new Line(2.0, 3.0, 4.0)};
          LineSegment[] lineSegments = {new LineSegment(new Point(1.0, 1.0), new Point(-3.0, -2.0)), new LineSegment(new Point(2.0, 2.0), new Point(3.0, 3.0))};
          Box[] boxes = {new Box(new Point(2.0, 2.0), new Point(1.0, 1.0)), new Box(new Point(4.0, 4.0), new Point(2.0, 2.0))};
          Path[] openPaths = {new Path(true, Arrays.asList(new Point(1.0, 1.0), new Point(2.0, 1.0), new Point(2.0, 2.0), new Point(2.0, 1.0))),
            new Path(false, Arrays.asList(new Point(2.0, 2.0), new Point(3.0, 2.0), new Point(3.0, 3.0), new Point(3.0, 2.0)))};
          Path[] closedPaths = {new Path(false, Arrays.asList(new Point(1.0, 1.0), new Point(2.0, 1.0), new Point(2.0, 2.0), new Point(2.0, 1.0))),
            new Path(false, Arrays.asList(new Point(2.0, 2.0), new Point(3.0, 2.0), new Point(3.0, 3.0), new Point(3.0, 2.0)))};
          Polygon[] polygons = {new Polygon(Arrays.asList(new Point(0.0, 0.0), new Point(2.0, 2.0), new Point(3.0, 1.0))),
            new Polygon(Arrays.asList(new Point(0.0, 0.0), new Point(0.0, 1.0), new Point(1.0, 2.0), new Point(2.0, 1.0), new Point(2.0, 0.0)))};
          Circle[] circles = {new Circle(new Point(1.0, 1.0), 3.0), new Circle(new Point(2.0, 2.0), 2.0)};
          int id = 2;
          p.execute(Tuple.tuple()
            .addPointArray(points)
            .addLineArray(lines)
            .addLineSegmentArray(lineSegments)
            .addBoxArray(boxes)
            .addPathArray(openPaths)
            .addPathArray(closedPaths)
            .addPolygonArray(polygons)
            .addCircleArray(circles)
            .addInteger(id), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            ColumnChecker.checkColumn(0, "Point")
              .returns(Tuple::getValue, Row::getValue, points)
              .returns(Tuple::getPointArray, Row::getPointArray, points)
              .forRow(row);
            ColumnChecker.checkColumn(1, "Line")
              .returns(Tuple::getValue, Row::getValue, lines)
              .returns(Tuple::getLineArray, Row::getLineArray, lines)
              .forRow(row);
            ColumnChecker.checkColumn(2, "Lseg")
              .returns(Tuple::getValue, Row::getValue, lineSegments)
              .returns(Tuple::getLineSegmentArray, Row::getLineSegmentArray, lineSegments)
              .forRow(row);
            ColumnChecker.checkColumn(3, "Box")
              .returns(Tuple::getValue, Row::getValue, boxes)
              .returns(Tuple::getBoxArray, Row::getBoxArray, boxes)
              .forRow(row);
            ColumnChecker.checkColumn(4, "ClosedPath")
              .returns(Tuple::getValue, Row::getValue, openPaths)
              .returns(Tuple::getPathArray, Row::getPathArray, openPaths)
              .forRow(row);
            ColumnChecker.checkColumn(5, "OpenPath")
              .returns(Tuple::getValue, Row::getValue, closedPaths)
              .returns(Tuple::getPathArray, Row::getPathArray, closedPaths)
              .forRow(row);
            ColumnChecker.checkColumn(6, "Polygon")
              .returns(Tuple::getValue, Row::getValue, polygons)
              .returns(Tuple::getPolygonArray, Row::getPolygonArray, polygons)
              .forRow(row);
            ColumnChecker.checkColumn(7, "Circle")
              .returns(Tuple::getValue, Row::getValue, circles)
              .returns(Tuple::getCircleArray, Row::getCircleArray, circles)
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }
}

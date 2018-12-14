package io.reactiverse.pgclient.data;

import io.reactiverse.pgclient.PgClient;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.Arrays;

public class GeometricTypesExtendedCodecTest extends ExtendedQueryDataTypeCodecTestBase {
  @Test
  public void testDecodePoint(TestContext ctx) {
    testGeneric(ctx, "SELECT $1 :: POINT \"Point\"", new Point[]{new Point(1.0, 2.0)}, Row::getPoint);
  }

  @Test
  public void testDecodeLine(TestContext ctx) {
    testGeneric(ctx, "SELECT $1 :: LINE \"Line\"", new Line[]{new Line(1.0, 2.0, 3.0)}, Row::getLine);
  }

  @Test
  public void testDecodeLineSegment(TestContext ctx) {
    testGeneric(ctx, "SELECT $1 :: LSEG \"Lseg\"", new LineSegment[]{new LineSegment(new Point(1.0, 1.0), new Point(2.0, 2.0))}, Row::getLineSegment);
  }

  @Test
  public void testDecodeBox(TestContext ctx) {
    testGeneric(ctx, "SELECT $1 :: BOX \"Box\"", new Box[]{new Box(new Point(2.0, 2.0), new Point(1.0, 1.0))}, Row::getBox);
  }

  @Test
  public void testDecodeClosedPath(TestContext ctx) {
    testGeneric(ctx, "SELECT $1 :: PATH \"ClosedPath\"", new Path[]{new Path(false, Arrays.asList(new Point(1.0, 1.0), new Point(2.0, 1.0), new Point(2.0, 2.0), new Point(2.0, 1.0)))}, Row::getPath);
  }

  @Test
  public void testDecodeOpenPath(TestContext ctx) {
    testGeneric(ctx, "SELECT $1 :: PATH \"OpenPath\"", new Path[]{new Path(true, Arrays.asList(new Point(1.0, 1.0), new Point(2.0, 1.0), new Point(2.0, 2.0), new Point(2.0, 1.0)))}, Row::getPath);
  }

  @Test
  public void testDecodePolygon(TestContext ctx) {
    testGeneric(ctx, "SELECT $1 :: POLYGON \"Polygon\"", new Polygon[]{new Polygon(Arrays.asList(new Point(1.0, 1.0), new Point(2.0, 2.0), new Point(3.0, 1.0)))}, Row::getPolygon);
  }

  @Test
  public void testDecodeCircle(TestContext ctx) {
    testGeneric(ctx, "SELECT $1 :: CIRCLE \"Circle\"", new Circle[]{new Circle(new Point(1.0, 1.0), 1.0)}, Row::getCircle);
  }

  @Test
  public void testDecodePointArray(TestContext ctx) {
    Point[] points = {new Point(1.0, 1.0), new Point(2.0, 2.0)};
    testGeneric(ctx, "SELECT $1 :: POINT[] \"PointArray\"", new Point[][]{points}, Row::getPointArray);
  }

  @Test
  public void testDecodeLineArray(TestContext ctx) {
    Line[] lines = {new Line(1.0, 2.0, 3.0), new Line(2.0, 3.0, 4.0)};
    testGeneric(ctx, "SELECT $1 :: LINE[] \"LineArray\"", new Line[][]{lines}, Row::getLineArray);
  }

  @Test
  public void testDecodeLineSegmentArray(TestContext ctx) {
    LineSegment[] lineSegments = {new LineSegment(new Point(1.0, 1.0), new Point(2.0, 2.0)), new LineSegment(new Point(2.0, 2.0), new Point(3.0, 3.0))};
    testGeneric(ctx, "SELECT $1 :: LSEG[] \"LsegArray\"", new LineSegment[][]{lineSegments}, Row::getLineSegmentArray);
  }

  @Test
  public void testDecodeBoxArray(TestContext ctx) {
    Box[] boxes = {new Box(new Point(2.0, 2.0), new Point(1.0, 1.0)), new Box(new Point(3.0, 3.0), new Point(2.0, 2.0))};
    testGeneric(ctx, "SELECT $1 :: BOX[] \"BoxArray\"", new Box[][]{boxes}, Row::getBoxArray);
  }

  @Test
  public void testDecodeClosedPathArray(TestContext ctx) {
    Path[] closedPaths = {new Path(false, Arrays.asList(new Point(1.0, 1.0), new Point(2.0, 1.0), new Point(2.0, 2.0), new Point(2.0, 1.0))),
      new Path(false, Arrays.asList(new Point(2.0, 2.0), new Point(3.0, 2.0), new Point(3.0, 3.0), new Point(3.0, 2.0)))};
    testGeneric(ctx, "SELECT $1 :: PATH[] \"ClosedPathArray\"", new Path[][]{closedPaths}, Row::getPathArray);
  }

  @Test
  public void testDecodeOpenPathArray(TestContext ctx) {
    Path[] openPaths = {new Path(true, Arrays.asList(new Point(1.0, 1.0), new Point(2.0, 1.0), new Point(2.0, 2.0), new Point(2.0, 1.0))),
      new Path(true, Arrays.asList(new Point(2.0, 2.0), new Point(3.0, 2.0), new Point(3.0, 3.0), new Point(3.0, 2.0)))};
    testGeneric(ctx, "SELECT $1 :: PATH[] \"OpenPathArray\"", new Path[][]{openPaths}, Row::getPathArray);
  }

  @Test
  public void testDecodePolygonArray(TestContext ctx) {
    Polygon[] polygons = {new Polygon(Arrays.asList(new Point(1.0, 1.0), new Point(2.0, 2.0), new Point(3.0, 1.0))),
      new Polygon(Arrays.asList(new Point(0.0, 0.0), new Point(0.0, 1.0), new Point(1.0, 2.0), new Point(2.0, 1.0), new Point(2.0, 0.0)))};
    testGeneric(ctx, "SELECT $1 :: POLYGON[] \"PolygonArray\"", new Polygon[][]{polygons}, Row::getPolygonArray);
  }

  @Test
  public void testDecodeCircleArray(TestContext ctx) {
    Circle[] circles = {new Circle(new Point(1.0, 1.0), 1.0), new Circle(new Point(0.0, 0.0), 2.0)};
    testGeneric(ctx, "SELECT $1 :: CIRCLE[] \"CircleArray\"", new Circle[][]{circles}, Row::getCircleArray);
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

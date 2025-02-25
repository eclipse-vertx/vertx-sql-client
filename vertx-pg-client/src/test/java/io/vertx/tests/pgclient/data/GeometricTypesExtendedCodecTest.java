package io.vertx.tests.pgclient.data;

import io.vertx.pgclient.PgConnection;
import io.vertx.pgclient.data.*;
import io.vertx.tests.sqlclient.ColumnChecker;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.Arrays;

public class GeometricTypesExtendedCodecTest extends ExtendedQueryDataTypeCodecTestBase {
  @Test
  public void testDecodePoint(TestContext ctx) {
    testGeneric(ctx, "SELECT $1 :: POINT \"Point\"", new Point[]{new Point(1.0, 2.0)}, Point.class);
  }

  @Test
  public void testDecodeLine(TestContext ctx) {
    testGeneric(ctx, "SELECT $1 :: LINE \"Line\"", new Line[]{new Line(1.0, 2.0, 3.0)}, Line.class);
  }

  @Test
  public void testDecodeLineSegment(TestContext ctx) {
    testGeneric(ctx, "SELECT $1 :: LSEG \"Lseg\"", new LineSegment[]{new LineSegment(new Point(1.0, 1.0), new Point(2.0, 2.0))}, LineSegment.class);
  }

  @Test
  public void testDecodeBox(TestContext ctx) {
    testGeneric(ctx, "SELECT $1 :: BOX \"Box\"", new Box[]{new Box(new Point(2.0, 2.0), new Point(1.0, 1.0))}, Box.class);
  }

  @Test
  public void testDecodeClosedPath(TestContext ctx) {
    testGeneric(ctx, "SELECT $1 :: PATH \"ClosedPath\"", new Path[]{new Path(false, Arrays.asList(new Point(1.0, 1.0), new Point(2.0, 1.0), new Point(2.0, 2.0), new Point(2.0, 1.0)))}, Path.class);
  }

  @Test
  public void testDecodeOpenPath(TestContext ctx) {
    testGeneric(ctx, "SELECT $1 :: PATH \"OpenPath\"", new Path[]{new Path(true, Arrays.asList(new Point(1.0, 1.0), new Point(2.0, 1.0), new Point(2.0, 2.0), new Point(2.0, 1.0)))}, Path.class);
  }

  @Test
  public void testDecodePolygon(TestContext ctx) {
    testGeneric(ctx, "SELECT $1 :: POLYGON \"Polygon\"", new Polygon[]{new Polygon(Arrays.asList(new Point(1.0, 1.0), new Point(2.0, 2.0), new Point(3.0, 1.0)))}, Polygon.class);
  }

  @Test
  public void testDecodeCircle(TestContext ctx) {
    testGeneric(ctx, "SELECT $1 :: CIRCLE \"Circle\"", new Circle[]{new Circle(new Point(1.0, 1.0), 1.0)}, Circle.class);
  }

  @Test
  public void testDecodePointArray(TestContext ctx) {
    Point[] points = {new Point(1.0, 1.0), new Point(2.0, 2.0)};
    testGenericArray(ctx, "SELECT $1 :: POINT[] \"PointArray\"", new Point[][]{points}, Point.class);
  }

  @Test
  public void testDecodeLineArray(TestContext ctx) {
    Line[] lines = {new Line(1.0, 2.0, 3.0), new Line(2.0, 3.0, 4.0)};
    testGenericArray(ctx, "SELECT $1 :: LINE[] \"LineArray\"", new Line[][]{lines}, Line.class);
  }

  @Test
  public void testDecodeLineSegmentArray(TestContext ctx) {
    LineSegment[] lineSegments = {new LineSegment(new Point(1.0, 1.0), new Point(2.0, 2.0)), new LineSegment(new Point(2.0, 2.0), new Point(3.0, 3.0))};
    testGenericArray(ctx, "SELECT $1 :: LSEG[] \"LsegArray\"", new LineSegment[][]{lineSegments}, LineSegment.class);
  }

  @Test
  public void testDecodeBoxArray(TestContext ctx) {
    Box[] boxes = {new Box(new Point(2.0, 2.0), new Point(1.0, 1.0)), new Box(new Point(3.0, 3.0), new Point(2.0, 2.0))};
    testGenericArray(ctx, "SELECT $1 :: BOX[] \"BoxArray\"", new Box[][]{boxes}, Box.class);
  }

  @Test
  public void testDecodeClosedPathArray(TestContext ctx) {
    Path[] closedPaths = {new Path(false, Arrays.asList(new Point(1.0, 1.0), new Point(2.0, 1.0), new Point(2.0, 2.0), new Point(2.0, 1.0))),
      new Path(false, Arrays.asList(new Point(2.0, 2.0), new Point(3.0, 2.0), new Point(3.0, 3.0), new Point(3.0, 2.0)))};
    testGenericArray(ctx, "SELECT $1 :: PATH[] \"ClosedPathArray\"", new Path[][]{closedPaths}, Path.class);
  }

  @Test
  public void testDecodeOpenPathArray(TestContext ctx) {
    Path[] openPaths = {new Path(true, Arrays.asList(new Point(1.0, 1.0), new Point(2.0, 1.0), new Point(2.0, 2.0), new Point(2.0, 1.0))),
      new Path(true, Arrays.asList(new Point(2.0, 2.0), new Point(3.0, 2.0), new Point(3.0, 3.0), new Point(3.0, 2.0)))};
    testGenericArray(ctx, "SELECT $1 :: PATH[] \"OpenPathArray\"", new Path[][]{openPaths}, Path.class);
  }

  @Test
  public void testDecodePolygonArray(TestContext ctx) {
    Polygon[] polygons = {new Polygon(Arrays.asList(new Point(1.0, 1.0), new Point(2.0, 2.0), new Point(3.0, 1.0))),
      new Polygon(Arrays.asList(new Point(0.0, 0.0), new Point(0.0, 1.0), new Point(1.0, 2.0), new Point(2.0, 1.0), new Point(2.0, 0.0)))};
    testGenericArray(ctx, "SELECT $1 :: POLYGON[] \"PolygonArray\"", new Polygon[][]{polygons}, Polygon.class);
  }

  @Test
  public void testDecodeCircleArray(TestContext ctx) {
    Circle[] circles = {new Circle(new Point(1.0, 1.0), 1.0), new Circle(new Point(0.0, 0.0), 2.0)};
    testGenericArray(ctx, "SELECT $1 :: CIRCLE[] \"CircleArray\"", new Circle[][]{circles}, Circle.class);
  }

  @Test
  public void testEncodeGeometric(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .prepare("UPDATE \"" + "GeometricDataType" + "\" SET " +
          "\"Point\" = $1, " +
          "\"Line\" = $2, " +
          "\"Lseg\" = $3, " +
          "\"Box\" = $4, " +
          "\"ClosedPath\" = $5, " +
          "\"OpenPath\" = $6, " +
          "\"Polygon\" = $7, " +
          "\"Circle\" = $8 " +
          "WHERE \"id\" = $9 RETURNING \"Point\", \"Line\", \"Lseg\", \"Box\", \"ClosedPath\", \"OpenPath\", \"Polygon\", \"Circle\"")
        .onComplete(ctx.asyncAssertSuccess(p -> {
          Point point = new Point(2.0, 3.0);
          Line line = new Line(2.0, 3.0, 4.0);
          LineSegment lineSegment = new LineSegment(new Point(2.0, 2.0), new Point(3.0, 3.0));
          Box box = new Box(new Point(3.0, 3.0), new Point(2.0, 2.0));
          Path OpenPath = new Path(true, Arrays.asList(new Point(1.0, 1.0), new Point(2.0, 1.0), new Point(2.0, 2.0), new Point(2.0, 1.0)));
          Path closedPath = new Path(false, Arrays.asList(new Point(1.0, 1.0), new Point(2.0, 1.0), new Point(2.0, 2.0), new Point(2.0, 1.0)));
          Polygon polygon = new Polygon(Arrays.asList(new Point(2.0, 2.0), new Point(3.0, 3.0), new Point(4.0, 2.0)));
          Circle circle = new Circle(new Point(1.0, 1.0), 3.0);
          int id = 2;
          p.query()
            .execute(Tuple.tuple()
            .addValue(point)
            .addValue(line)
            .addValue(lineSegment)
            .addValue(box)
            .addValue(OpenPath)
            .addValue(closedPath)
            .addValue(polygon)
            .addValue(circle)
            .addInteger(id))
            .onComplete(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            ColumnChecker.checkColumn(0, "Point")
              .returns(Tuple::getValue, Row::getValue, point)
              .returns(Point.class, point)
              .forRow(row);
            ColumnChecker.checkColumn(1, "Line")
              .returns(Tuple::getValue, Row::getValue, line)
              .returns(Line.class, line)
              .forRow(row);
            ColumnChecker.checkColumn(2, "Lseg")
              .returns(Tuple::getValue, Row::getValue, lineSegment)
              .returns(LineSegment.class, lineSegment)
              .forRow(row);
            ColumnChecker.checkColumn(3, "Box")
              .returns(Tuple::getValue, Row::getValue, box)
              .returns(Box.class, box)
              .forRow(row);
            ColumnChecker.checkColumn(4, "ClosedPath")
              .returns(Tuple::getValue, Row::getValue, OpenPath)
              .returns(Path.class, OpenPath)
              .forRow(row);
            ColumnChecker.checkColumn(5, "OpenPath")
              .returns(Tuple::getValue, Row::getValue, closedPath)
              .returns(Path.class, closedPath)
              .forRow(row);
            ColumnChecker.checkColumn(6, "Polygon")
              .returns(Tuple::getValue, Row::getValue, polygon)
              .returns(Polygon.class, polygon)
              .forRow(row);
            ColumnChecker.checkColumn(7, "Circle")
              .returns(Tuple::getValue, Row::getValue, circle)
              .returns(Circle.class, circle)
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeGeometricArray(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .prepare("UPDATE \"" + "ArrayDataType" + "\" SET " +
          "\"Point\" = $1, " +
          "\"Line\" = $2, " +
          "\"Lseg\" = $3, " +
          "\"Box\" = $4, " +
          "\"ClosedPath\" = $5, " +
          "\"OpenPath\" = $6, " +
          "\"Polygon\" = $7, " +
          "\"Circle\" = $8 " +
          "WHERE \"id\" = $9 RETURNING \"Point\", \"Line\", \"Lseg\", \"Box\", \"ClosedPath\", \"OpenPath\", \"Polygon\", \"Circle\"")
        .onComplete(ctx.asyncAssertSuccess(p -> {
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
          p
            .query()
            .execute(Tuple.tuple()
            .addValue(points)
            .addValue(lines)
            .addValue(lineSegments)
            .addValue(boxes)
            .addValue(openPaths)
            .addValue(closedPaths)
            .addValue(polygons)
            .addValue(circles)
            .addInteger(id))
            .onComplete(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            ColumnChecker.checkColumn(0, "Point")
              .returns(Tuple::getValue, Row::getValue, points)
              .returns(Point.class, points)
              .forRow(row);
            ColumnChecker.checkColumn(1, "Line")
              .returns(Tuple::getValue, Row::getValue, lines)
              .returns(Line.class, lines)
              .forRow(row);
            ColumnChecker.checkColumn(2, "Lseg")
              .returns(Tuple::getValue, Row::getValue, lineSegments)
              .returns(LineSegment.class, lineSegments)
              .forRow(row);
            ColumnChecker.checkColumn(3, "Box")
              .returns(Tuple::getValue, Row::getValue, boxes)
              .returns(Box.class, boxes)
              .forRow(row);
            ColumnChecker.checkColumn(4, "ClosedPath")
              .returns(Tuple::getValue, Row::getValue, openPaths)
              .returns(Path.class, openPaths)
              .forRow(row);
            ColumnChecker.checkColumn(5, "OpenPath")
              .returns(Tuple::getValue, Row::getValue, closedPaths)
              .returns(Path.class, closedPaths)
              .forRow(row);
            ColumnChecker.checkColumn(6, "Polygon")
              .returns(Tuple::getValue, Row::getValue, polygons)
              .returns(Polygon.class, polygons)
              .forRow(row);
            ColumnChecker.checkColumn(7, "Circle")
              .returns(Tuple::getValue, Row::getValue, circles)
              .returns(Circle.class, circles)
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }
}

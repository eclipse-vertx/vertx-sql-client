package io.reactiverse.pgclient.codec.simple;

import io.reactiverse.pgclient.PgClient;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.reactiverse.pgclient.codec.ColumnChecker;
import io.reactiverse.pgclient.codec.SimpleQueryDataTypeCodecTestBase;
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

public class GeometricTypesTest extends SimpleQueryDataTypeCodecTestBase {
  @Test
  public void testPoint(TestContext ctx) {
    Point expected = new Point(1.0, 2.0);
    testDecodeGeneric(ctx, "(1.0,2.0)", "POINT", "Point", Tuple::getPoint, Row::getPoint, expected);
  }

  @Test
  public void testLine(TestContext ctx) {
    Line expected = new Line(1.0, 2.0, 3.0);
    testDecodeGeneric(ctx, "{1.0,2.0,3.0}", "LINE", "Line", Tuple::getLine, Row::getLine, expected);
  }

  @Test
  public void testLineSegment(TestContext ctx) {
    LineSegment expected = new LineSegment(new Point(1.0, 1.0), new Point(2.0, 2.0));
    testDecodeGeneric(ctx, "((1.0,1.0),(2.0,2.0))", "LSEG", "Lseg", Tuple::getLineSegment, Row::getLineSegment, expected);
  }

  @Test
  public void testBox(TestContext ctx) {
    Box expected = new Box(new Point(2.0, 2.0), new Point(1.0, 1.0));
    testDecodeGeneric(ctx, "((2.0,2.0),(1.0,1.0))", "BOX", "Box", Tuple::getBox, Row::getBox, expected);
  }

  @Test
  public void testClosedPath(TestContext ctx) {
    Path expected = new Path(false, Arrays.asList(new Point(1.0, 1.0), new Point(2.0, 1.0), new Point(2.0, 2.0), new Point(2.0, 1.0)));
    testDecodeGeneric(ctx, "((1.0,1.0),(2.0,1.0),(2.0,2.0),(2.0,1.0))", "PATH", "ClosedPath", Tuple::getPath, Row::getPath, expected);
  }

  @Test
  public void testOpenPath(TestContext ctx) {
    Path expected = new Path(true, Arrays.asList(new Point(1.0, 1.0), new Point(2.0, 1.0), new Point(2.0, 2.0), new Point(2.0, 1.0)));
    testDecodeGeneric(ctx, "[(1.0,1.0),(2.0,1.0),(2.0,2.0),(2.0,1.0)]", "PATH", "OpenPath", Tuple::getPath, Row::getPath, expected);
  }

  @Test
  public void testPolygon(TestContext ctx) {
    Polygon expected = new Polygon(Arrays.asList(new Point(1.0, 1.0), new Point(2.0, 2.0), new Point(3.0, 1.0)));
    testDecodeGeneric(ctx, "((1.0,1.0),(2.0,2.0),(3.0,1.0))", "POLYGON", "Polygon", Tuple::getPolygon, Row::getPolygon, expected);
  }

  @Test
  public void testCircle(TestContext ctx) {
    Circle expected = new Circle(new Point(1.0, 1.0), 1.0);
    testDecodeGeneric(ctx, "<(1.0,1.0),1.0>", "CIRCLE", "Circle", Tuple::getCircle, Row::getCircle, expected);
  }

  @Test
  public void testGeometricArray(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT \"Point\", \"Line\", \"Lseg\", \"Box\", \"ClosedPath\", \"OpenPath\", \"Polygon\", \"Circle\" FROM \"ArrayDataType\" WHERE \"id\" = 1",
          ctx.asyncAssertSuccess(result -> {
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
  }
}

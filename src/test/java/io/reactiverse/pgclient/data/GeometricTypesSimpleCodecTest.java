package io.reactiverse.pgclient.data;

import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.Arrays;

public class GeometricTypesSimpleCodecTest extends SimpleQueryDataTypeCodecTestBase {
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
  public void testPointArray(TestContext ctx) {
    Point[] expected = {new Point(1.0, 1.0), new Point(2.0, 2.0)};
    testDecodeGenericArray(ctx, "ARRAY ['(1.0,1.0)':: POINT, '(2.0,2.0)' :: POINT]", "Point", Tuple::getPointArray, Row::getPointArray, expected);
  }

  @Test
  public void testLineArray(TestContext ctx) {
    Line[] expected = {new Line(1.0, 2.0, 3.0), new Line(2.0, 3.0, 4.0)};
    testDecodeGenericArray(ctx, "ARRAY ['{1.0,2.0,3.0}':: LINE, '{2.0,3.0,4.0}':: LINE]", "Line", Tuple::getLineArray, Row::getLineArray, expected);
  }

  @Test
  public void testLineSegmentArray(TestContext ctx) {
    LineSegment[] expected = {new LineSegment(new Point(1.0, 1.0), new Point(2.0, 2.0)), new LineSegment(new Point(2.0, 2.0), new Point(3.0, 3.0))};
    testDecodeGenericArray(ctx, "ARRAY ['((1.0,1.0),(2.0,2.0))':: LSEG, '((2.0,2.0),(3.0,3.0))':: LSEG]", "Lseg", Tuple::getLineSegmentArray, Row::getLineSegmentArray, expected);
  }

  @Test
  public void testBoxArray(TestContext ctx) {
    Box[] expected = {new Box(new Point(2.0, 2.0), new Point(1.0, 1.0)), new Box(new Point(3.0, 3.0), new Point(2.0, 2.0))};
    testDecodeGenericArray(ctx, "ARRAY ['((2.0,2.0),(1.0,1.0))':: BOX, '((3.0,3.0),(2.0,2.0))':: BOX]", "Box", Tuple::getBoxArray, Row::getBoxArray, expected);
  }

  @Test
  public void testClosedPathArray(TestContext ctx) {
    Path[] expected = {new Path(false, Arrays.asList(new Point(1.0, 1.0), new Point(2.0, 1.0), new Point(2.0, 2.0), new Point(2.0, 1.0))),
      new Path(false, Arrays.asList(new Point(2.0, 2.0), new Point(3.0, 2.0), new Point(3.0, 3.0), new Point(3.0, 2.0)))};
    testDecodeGenericArray(ctx, "ARRAY ['((1.0,1.0),(2.0,1.0),(2.0,2.0),(2.0,1.0))':: PATH, '((2.0,2.0),(3.0,2.0),(3.0,3.0),(3.0,2.0))':: PATH]", "ClosedPath", Tuple::getPathArray, Row::getPathArray, expected);
  }

  @Test
  public void testOpenPathArray(TestContext ctx) {
    Path[] expected = {new Path(true, Arrays.asList(new Point(1.0, 1.0), new Point(2.0, 1.0), new Point(2.0, 2.0), new Point(2.0, 1.0))),
      new Path(true, Arrays.asList(new Point(2.0, 2.0), new Point(3.0, 2.0), new Point(3.0, 3.0), new Point(3.0, 2.0)))};
    testDecodeGenericArray(ctx, "ARRAY ['[(1.0,1.0),(2.0,1.0),(2.0,2.0),(2.0,1.0)]':: PATH, '[(2.0,2.0),(3.0,2.0),(3.0,3.0),(3.0,2.0)]':: PATH]", "OpenPath", Tuple::getPathArray, Row::getPathArray, expected);
  }

  @Test
  public void testPolygonArray(TestContext ctx) {
    Polygon[] expected = {new Polygon(Arrays.asList(new Point(1.0, 1.0), new Point(2.0, 2.0), new Point(3.0, 1.0))),
      new Polygon(Arrays.asList(new Point(0.0, 0.0), new Point(0.0, 1.0), new Point(1.0, 2.0), new Point(2.0, 1.0), new Point(2.0, 0.0)))};
    testDecodeGenericArray(ctx, "ARRAY ['((1.0,1.0),(2.0,2.0),(3.0,1.0))':: POLYGON, '((0.0,0.0),(0.0,1.0),(1.0,2.0),(2.0,1.0),(2.0,0.0))':: POLYGON]", "Polygon", Tuple::getPolygonArray, Row::getPolygonArray, expected);
  }

  @Test
  public void testCircleArray(TestContext ctx) {
    Circle[] expected = {new Circle(new Point(1.0, 1.0), 1.0), new Circle(new Point(0.0, 0.0), 2.0)};
    testDecodeGenericArray(ctx, "ARRAY ['<(1.0,1.0),1.0>':: CIRCLE, '<(0.0,0.0),2.0>':: CIRCLE]", "Circle", Tuple::getCircleArray, Row::getCircleArray, expected);
  }
}

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

package io.vertx.mysqlclient.impl.datatype;

import io.netty.buffer.ByteBuf;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.mysqlclient.data.spatial.*;
import io.vertx.mysqlclient.impl.util.BufferUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * An Well-known Binary(WKB) format codec based on OpenGIS specification
 */
public class GeometryWkbFormatCodec {
  private static final Logger LOGGER = LoggerFactory.getLogger(GeometryWkbFormatCodec.class);

  private static final byte WKB_BYTE_ORDER_LITTLE_ENDIAN = 1;

  private static final int WKB_GEOMETRY_TYPE_POINT = 1;
  private static final int WKB_GEOMETRY_TYPE_LINESTRING = 2;
  private static final int WKB_GEOMETRY_TYPE_POLYGON = 3;
  private static final int WKB_GEOMETRY_TYPE_MULTIPOINT = 4;
  private static final int WKB_GEOMETRY_TYPE_MULTILINESTRING = 5;
  private static final int WKB_GEOMETRY_TYPE_MULTIPOLYGON = 6;
  private static final int WKB_GEOMETRY_TYPE_GEOMETRYCOLLECTION = 7;

  public static Object decodeMySQLGeometry(ByteBuf buffer) {
    long srid = buffer.readUnsignedIntLE();
    buffer.readByte(); // byteOrder, always Little-endian for MySQL
    int type = buffer.readIntLE();
    return decodeWkbFormatGeometry(buffer, srid, type);
  }

  public static void encodeGeometryToMySQLBlob(ByteBuf buffer, Geometry geometry) {
    if (geometry instanceof Point) {
      encodePointToBlob(buffer, (Point) geometry);
    } else if (geometry instanceof LineString) {
      encodeLineStringToBlob(buffer, (LineString) geometry);
    } else if (geometry instanceof Polygon) {
      encodePolygonToBlob(buffer, (Polygon) geometry);
    } else if (geometry instanceof MultiPoint) {
      encodeMultiPointToBlob(buffer, (MultiPoint) geometry);
    } else if (geometry instanceof MultiLineString) {
      encodeMultiLineStringToBlob(buffer, (MultiLineString) geometry);
    } else if (geometry instanceof MultiPolygon) {
      encodeMultiPolygonToBlob(buffer, (MultiPolygon) geometry);
    } else if (geometry instanceof GeometryCollection) {
      encodeGeometryCollectionToBlob(buffer, (GeometryCollection) geometry);
    } else {
      LOGGER.error(String.format("Error when encoding unknown geometry type, type=[%s]", geometry.getClass().getName()));
    }
  }

  private static Object decodeWkbFormatGeometry(ByteBuf buffer, long srid, int type) {
    switch (type) {
      case WKB_GEOMETRY_TYPE_POINT:
        return decodePoint(buffer, srid);
      case WKB_GEOMETRY_TYPE_LINESTRING:
        return decodeLineString(buffer, srid);
      case WKB_GEOMETRY_TYPE_POLYGON:
        return decodePolygon(buffer, srid);
      case WKB_GEOMETRY_TYPE_MULTIPOINT:
        return decodeMultiPoint(buffer, srid);
      case WKB_GEOMETRY_TYPE_MULTILINESTRING:
        return decodeMultiLineString(buffer, srid);
      case WKB_GEOMETRY_TYPE_MULTIPOLYGON:
        return decodeMultiPolygon(buffer, srid);
      case WKB_GEOMETRY_TYPE_GEOMETRYCOLLECTION:
        return decodeGeometryCollection(buffer, srid);
      default:
        LOGGER.error(String.format("Error when parsing unknown geometry data type, wkbTypeId=[%d]", type));
        return null;
    }
  }

  private static Point decodePoint(ByteBuf buffer, long srid) {
    double x = buffer.readDoubleLE();
    double y = buffer.readDoubleLE();
    return new Point(srid, x, y);
  }

  private static LineString decodeLineString(ByteBuf buffer, long srid) {
    long numPoints = buffer.readUnsignedIntLE();
    List<Point> points = new ArrayList<>();
    for (long i = 0; i < numPoints; i++) {
      Point point = decodePoint(buffer, srid);
      points.add(point);
    }
    return new LineString(srid, points);
  }

  private static Polygon decodePolygon(ByteBuf buffer, long srid) {
    long numRings = buffer.readUnsignedIntLE();
    List<LineString> rings = new ArrayList<>();
    for (long i = 0; i < numRings; i++) {
      LineString linearRing = decodeLineString(buffer, srid);
      rings.add(linearRing);
    }
    return new Polygon(srid, rings);
  }

  private static MultiPoint decodeMultiPoint(ByteBuf buffer, long srid) {
    long numWkbPoints = buffer.readUnsignedIntLE();
    List<Point> wkbPoints = new ArrayList<>();
    for (long i = 0; i < numWkbPoints; i++) {
      buffer.skipBytes(5);
      Point wkbPoint = decodePoint(buffer, srid);
      wkbPoints.add(wkbPoint);
    }
    return new MultiPoint(srid, wkbPoints);
  }

  private static MultiLineString decodeMultiLineString(ByteBuf buffer, long srid) {
    long numWkbLineStrings = buffer.readUnsignedIntLE();
    List<LineString> wkbLineStrings = new ArrayList<>();
    for (long i = 0; i < numWkbLineStrings; i++) {
      buffer.skipBytes(5);
      LineString linearRing = decodeLineString(buffer, srid);
      wkbLineStrings.add(linearRing);
    }
    return new MultiLineString(srid, wkbLineStrings);
  }

  private static MultiPolygon decodeMultiPolygon(ByteBuf buffer, long srid) {
    long numWkbPolygons = buffer.readUnsignedIntLE();
    List<Polygon> wkbPolygons = new ArrayList<>();
    for (long i = 0; i < numWkbPolygons; i++) {
      buffer.skipBytes(5);
      Polygon wkbPolygon = decodePolygon(buffer, srid);
      wkbPolygons.add(wkbPolygon);
    }
    return new MultiPolygon(srid, wkbPolygons);
  }

  private static GeometryCollection decodeGeometryCollection(ByteBuf buffer, long srid) {
    long numWkbGeometries = buffer.readUnsignedIntLE();
    List<Geometry> wkbGeometries = new ArrayList<>();
    for (long i = 0; i < numWkbGeometries; i++) {
      buffer.skipBytes(1);
      int type = buffer.readIntLE();
      Geometry geometry = (Geometry) decodeWkbFormatGeometry(buffer, srid, type);
      wkbGeometries.add(geometry);
    }
    return new GeometryCollection(srid, wkbGeometries);
  }

  private static void encodePointToBlob(ByteBuf buffer, Point point) {
    BufferUtils.writeLengthEncodedInteger(buffer, 21);
    encodeWkbPoint(buffer, point);
  }

  private static void encodeLineStringToBlob(ByteBuf buffer, LineString lineString) {
    int bufferLength = calculateWkbLineStringLength(lineString);
    BufferUtils.writeLengthEncodedInteger(buffer, bufferLength);
    encodeWkbLineString(buffer, lineString);
  }

  private static void encodePolygonToBlob(ByteBuf buffer, Polygon polygon) {
    int bufferLength = calculateWkbPolygonLength(polygon);
    BufferUtils.writeLengthEncodedInteger(buffer, bufferLength);
    encodeWkbPolygon(buffer, polygon);
  }

  private static void encodeMultiPointToBlob(ByteBuf buffer, MultiPoint multiPoint) {
    int bufferLength = calculateWkbMultiPointLength(multiPoint);
    BufferUtils.writeLengthEncodedInteger(buffer, bufferLength);
    encodeWkbMultiPoint(buffer, multiPoint);
  }

  private static void encodeMultiLineStringToBlob(ByteBuf buffer, MultiLineString multiLineString) {
    int bufferLength = calculateWkbMultiLineStringLength(multiLineString);
    BufferUtils.writeLengthEncodedInteger(buffer, bufferLength);
    encodeWkbMultiLineString(buffer, multiLineString);
  }

  private static void encodeMultiPolygonToBlob(ByteBuf buffer, MultiPolygon multiPolygon) {
    int bufferLength = calculateWkbMultiPolygonLength(multiPolygon);
    BufferUtils.writeLengthEncodedInteger(buffer, bufferLength);
    encodeWkbMultiPolygon(buffer, multiPolygon);
  }

  private static void encodeGeometryCollectionToBlob(ByteBuf buffer, GeometryCollection geometryCollection) {
    int bufferLength = calculateWkbGeometryCollectionLength(geometryCollection);
    BufferUtils.writeLengthEncodedInteger(buffer, bufferLength);
    encodeWkbGeometryCollection(buffer, geometryCollection);
  }

  private static void encodeWkbGeometry(ByteBuf buffer, Geometry geometry) {
    if (geometry instanceof Point) {
      encodeWkbPoint(buffer, (Point) geometry);
    } else if (geometry instanceof LineString) {
      encodeWkbLineString(buffer, (LineString) geometry);
    } else if (geometry instanceof Polygon) {
      encodeWkbPolygon(buffer, (Polygon) geometry);
    } else if (geometry instanceof MultiPoint) {
      encodeWkbMultiPoint(buffer, (MultiPoint) geometry);
    } else if (geometry instanceof MultiLineString) {
      encodeWkbMultiLineString(buffer, (MultiLineString) geometry);
    } else if (geometry instanceof MultiPolygon) {
      encodeWkbMultiPolygon(buffer, (MultiPolygon) geometry);
    } else if (geometry instanceof GeometryCollection) {
      encodeWkbGeometryCollection(buffer, (GeometryCollection) geometry);
    } else {
      LOGGER.error("Unknown type of Geometry");
    }
  }

  private static void encodeWkbPoint(ByteBuf buffer, Point wkbPoint) {
    buffer.writeByte(WKB_BYTE_ORDER_LITTLE_ENDIAN);
    buffer.writeIntLE(WKB_GEOMETRY_TYPE_POINT);
    buffer.writeDoubleLE(wkbPoint.getX());
    buffer.writeDoubleLE(wkbPoint.getY());
  }

  private static void encodeWkbLineString(ByteBuf buffer, LineString wkbLineString) {
    buffer.writeByte(WKB_BYTE_ORDER_LITTLE_ENDIAN);
    buffer.writeIntLE(WKB_GEOMETRY_TYPE_LINESTRING);
    buffer.writeIntLE(wkbLineString.getPoints().size());
    for (Point point : wkbLineString.getPoints()) {
      buffer.writeDoubleLE(point.getX());
      buffer.writeDoubleLE(point.getY());
    }
  }

  private static void encodeWkbPolygon(ByteBuf buffer, Polygon polygon) {
    buffer.writeByte(WKB_BYTE_ORDER_LITTLE_ENDIAN);
    buffer.writeIntLE(WKB_GEOMETRY_TYPE_POLYGON);
    buffer.writeIntLE(polygon.getLineStrings().size());
    for (LineString lineString : polygon.getLineStrings()) {
      buffer.writeIntLE(lineString.getPoints().size());
      for (Point point : lineString.getPoints()) {
        buffer.writeDoubleLE(point.getX());
        buffer.writeDoubleLE(point.getY());
      }
    }
  }

  private static void encodeWkbMultiPoint(ByteBuf buffer, MultiPoint multiPoint) {
    buffer.writeByte(WKB_BYTE_ORDER_LITTLE_ENDIAN);
    buffer.writeIntLE(WKB_GEOMETRY_TYPE_MULTIPOINT);
    buffer.writeIntLE(multiPoint.getPoints().size());
    for (Point wkbPoint : multiPoint.getPoints()) {
      encodeWkbPoint(buffer, wkbPoint);
    }
  }

  private static void encodeWkbMultiLineString(ByteBuf buffer, MultiLineString multiLineString) {
    buffer.writeByte(WKB_BYTE_ORDER_LITTLE_ENDIAN);
    buffer.writeIntLE(WKB_GEOMETRY_TYPE_MULTILINESTRING);
    buffer.writeIntLE(multiLineString.getLineStrings().size());
    for (LineString wkbLineString : multiLineString.getLineStrings()) {
      encodeWkbLineString(buffer, wkbLineString);
    }
  }

  private static void encodeWkbMultiPolygon(ByteBuf buffer, MultiPolygon multiPolygon) {
    buffer.writeByte(WKB_BYTE_ORDER_LITTLE_ENDIAN);
    buffer.writeIntLE(WKB_GEOMETRY_TYPE_MULTIPOLYGON);
    buffer.writeIntLE(multiPolygon.getPolygons().size());
    for (Polygon wkbPolygon : multiPolygon.getPolygons()) {
      encodeWkbPolygon(buffer, wkbPolygon);
    }
  }

  private static void encodeWkbGeometryCollection(ByteBuf buffer, GeometryCollection geometryCollection) {
    buffer.writeByte(WKB_BYTE_ORDER_LITTLE_ENDIAN);
    buffer.writeIntLE(WKB_GEOMETRY_TYPE_GEOMETRYCOLLECTION);
    buffer.writeIntLE(geometryCollection.getGeometries().size());
    for (Geometry geometry : geometryCollection.getGeometries()) {
      encodeWkbGeometry(buffer, geometry);
    }
  }

  private static int calculateWkbLineStringLength(LineString lineString) {
    int numPoints = lineString.getPoints().size();
    return 1 + 4 + 4 + (numPoints * 16);
  }

  private static int calculateWkbPolygonLength(Polygon polygon) {
    int numOfTotalPoints = 0;
    for (LineString lineString : polygon.getLineStrings()) {
      numOfTotalPoints += lineString.getPoints().size();
    }
    return 1 + 4 + 4 + 4 * (polygon.getLineStrings().size()) + (numOfTotalPoints * 16);
  }

  private static int calculateWkbMultiPointLength(MultiPoint multiPoint) {
    int numWkbPoints = multiPoint.getPoints().size();
    return 1 + 4 + 4 + (numWkbPoints * 21);
  }

  private static int calculateWkbMultiLineStringLength(MultiLineString multiLineString) {
    int numWkbLineStrings = multiLineString.getLineStrings().size();
    int numOfTotalPoints = 0;
    for (LineString lineString : multiLineString.getLineStrings()) {
      numOfTotalPoints += lineString.getPoints().size();
    }
    return 1 + 4 + 4 + 9 * (numWkbLineStrings) + (numOfTotalPoints * 16);
  }

  private static int calculateWkbMultiPolygonLength(MultiPolygon multiPolygon) {
    int bufferLength = 1 + 4 + 4;
    for (Polygon polygon : multiPolygon.getPolygons()) {
      bufferLength += calculateWkbPolygonLength(polygon);
    }
    return bufferLength;
  }

  private static int calculateWkbGeometryCollectionLength(GeometryCollection geometryCollection) {
    int bufferLength = 1 + 4 + 4;
    for (Geometry geometry : geometryCollection.getGeometries()) {
      if (geometry instanceof Point) {
        bufferLength += 21;
      } else if (geometry instanceof LineString) {
        bufferLength += calculateWkbLineStringLength((LineString) geometry);
      } else if (geometry instanceof Polygon) {
        bufferLength += calculateWkbPolygonLength((Polygon) geometry);
      } else if (geometry instanceof MultiPoint) {
        bufferLength += calculateWkbMultiPointLength((MultiPoint) geometry);
      } else if (geometry instanceof MultiLineString) {
        bufferLength += calculateWkbMultiLineStringLength((MultiLineString) geometry);
      } else if (geometry instanceof MultiPolygon) {
        bufferLength += calculateWkbMultiPolygonLength((MultiPolygon) geometry);
      } else if (geometry instanceof GeometryCollection) {
        bufferLength += calculateWkbGeometryCollectionLength((GeometryCollection) geometry);
      } else {
        LOGGER.error("Unknown type of Geometry");
        return -1;
      }
    }
    return bufferLength;
  }

}

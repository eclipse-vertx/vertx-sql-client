/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.vertx.pgclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import io.vertx.core.spi.json.JsonCodec;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.data.Numeric;
import io.vertx.pgclient.data.*;
import io.vertx.pgclient.impl.util.UTF8StringEndDetector;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;
import static java.util.concurrent.TimeUnit.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
class DataTypeCodec {

  private static final Logger logger = LoggerFactory.getLogger(DataTypeCodec.class);

  private static final String[] empty_string_array = new String[0];
  private static final LocalDate[] empty_local_date_array = new LocalDate[0];
  private static final LocalTime[] empty_local_time_array = new LocalTime[0];
  private static final OffsetTime[] empty_offset_time_array = new OffsetTime[0];
  private static final LocalDateTime[] empty_local_date_time_array = new LocalDateTime[0];
  private static final OffsetDateTime[] empty_offset_date_time_array = new OffsetDateTime[0];
  private static final Buffer[] empty_buffer_array = new Buffer[0];
  private static final UUID[] empty_uuid_array = new UUID[0];
  private static final Object[] empty_json_array = new Object[0];
  private static final Numeric[] empty_numeric_array = new Numeric[0];
  private static final Point[] empty_point_array = new Point[0];
  private static final Line[] empty_line_array = new Line[0];
  private static final LineSegment[] empty_lseg_array = new LineSegment[0];
  private static final Box[] empty_box_array = new Box[0];
  private static final Path[] empty_path_array = new Path[0];
  private static final Polygon[] empty_polygon_array = new Polygon[0];
  private static final Circle[] empty_circle_array = new Circle[0];
  private static final Interval[] empty_interval_array = new Interval[0];
  private static final Boolean[] empty_boolean_array = new Boolean[0];
  private static final Integer[] empty_integer_array = new Integer[0];
  private static final Short[] empty_short_array = new Short[0];
  private static final Long[] empty_long_array = new Long[0];
  private static final Float[] empty_float_array = new Float[0];
  private static final Double[] empty_double_array = new Double[0];
  private static final LocalDate LOCAL_DATE_EPOCH = LocalDate.of(2000, 1, 1);
  private static final LocalDateTime LOCAL_DATE_TIME_EPOCH = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
  private static final OffsetDateTime OFFSET_DATE_TIME_EPOCH = LocalDateTime.of(2000, 1, 1, 0, 0, 0).atOffset(ZoneOffset.UTC);

  // Sentinel used when an object is refused by the data type
  public static final Object REFUSED_SENTINEL = new Object();

  private static final IntFunction<Boolean[]> BOOLEAN_ARRAY_FACTORY = size -> size == 0 ? empty_boolean_array : new Boolean[size];
  private static final IntFunction<Short[]> SHORT_ARRAY_FACTORY = size -> size == 0 ? empty_short_array : new Short[size];
  private static final IntFunction<Integer[]> INTEGER_ARRAY_FACTORY = size -> size == 0 ? empty_integer_array : new Integer[size];
  private static final IntFunction<Long[]> LONG_ARRAY_FACTORY = size -> size == 0 ? empty_long_array : new Long[size];
  private static final IntFunction<Float[]> FLOAT_ARRAY_FACTORY = size -> size == 0 ? empty_float_array : new Float[size];
  private static final IntFunction<Double[]> DOUBLE_ARRAY_FACTORY = size -> size == 0 ? empty_double_array : new Double[size];
  private static final IntFunction<String[]> STRING_ARRAY_FACTORY = size -> size == 0 ? empty_string_array : new String[size];
  private static final IntFunction<LocalDate[]> LOCALDATE_ARRAY_FACTORY = size -> size == 0 ? empty_local_date_array : new LocalDate[size];
  private static final IntFunction<LocalTime[]> LOCALTIME_ARRAY_FACTORY = size -> size == 0 ? empty_local_time_array : new LocalTime[size];
  private static final IntFunction<OffsetTime[]> OFFSETTIME_ARRAY_FACTORY = size -> size == 0 ? empty_offset_time_array : new OffsetTime[size];
  private static final IntFunction<LocalDateTime[]> LOCALDATETIME_ARRAY_FACTORY = size -> size == 0 ? empty_local_date_time_array : new LocalDateTime[size];
  private static final IntFunction<OffsetDateTime[]> OFFSETDATETIME_ARRAY_FACTORY = size -> size == 0 ? empty_offset_date_time_array : new OffsetDateTime[size];
  private static final IntFunction<Buffer[]> BUFFER_ARRAY_FACTORY =size -> size == 0 ? empty_buffer_array : new Buffer[size];
  private static final IntFunction<UUID[]> UUID_ARRAY_FACTORY = size -> size == 0 ? empty_uuid_array : new UUID[size];
  private static final IntFunction<Object[]> JSON_ARRAY_FACTORY = size -> size == 0 ? empty_json_array : new Object[size];
  private static final IntFunction<Numeric[]> NUMERIC_ARRAY_FACTORY = size -> size == 0 ? empty_numeric_array : new Numeric[size];
  private static final IntFunction<Point[]> POINT_ARRAY_FACTORY = size -> size == 0 ? empty_point_array : new Point[size];
  private static final IntFunction<Line[]> LINE_ARRAY_FACTORY = size -> size == 0 ? empty_line_array : new Line[size];
  private static final IntFunction<LineSegment[]> LSEG_ARRAY_FACTORY = size -> size == 0 ? empty_lseg_array : new LineSegment[size];
  private static final IntFunction<Box[]> BOX_ARRAY_FACTORY = size -> size == 0 ? empty_box_array : new Box[size];
  private static final IntFunction<Path[]> PATH_ARRAY_FACTORY = size -> size == 0 ? empty_path_array : new Path[size];
  private static final IntFunction<Polygon[]> POLYGON_ARRAY_FACTORY = size -> size == 0 ? empty_polygon_array : new Polygon[size];
  private static final IntFunction<Circle[]> CIRCLE_ARRAY_FACTORY = size -> size == 0 ? empty_circle_array : new Circle[size];
  private static final IntFunction<Interval[]> INTERVAL_ARRAY_FACTORY = size -> size == 0 ? empty_interval_array : new Interval[size];

  private static final java.time.format.DateTimeFormatter TIMETZ_FORMAT = new DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .append(ISO_LOCAL_TIME)
    .appendOffset("+HH:mm", "00:00")
    .toFormatter();

  private static final java.time.format.DateTimeFormatter TIMESTAMP_FORMAT = new DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .append(ISO_LOCAL_DATE)
    .appendLiteral(' ')
    .append(ISO_LOCAL_TIME)
    .toFormatter();

  private static final java.time.format.DateTimeFormatter TIMESTAMPTZ_FORMAT = new DateTimeFormatterBuilder()
    .append(TIMESTAMP_FORMAT)
    .appendOffset("+HH:mm", "00:00")
    .toFormatter();

  static void encodeText(DataType id, Object value, ByteBuf buff) {
    int index = buff.writerIndex();
    buff.writeInt(0);
    textEncode(id, value, buff);
    buff.setInt(index, buff.writerIndex() - index - 4);
  }

  private static void textEncode(DataType id, Object value, ByteBuf buff) {
    switch (id) {
      case NUMERIC:
        textEncodeNUMERIC((Number) value, buff);
        break;
      case NUMERIC_ARRAY:
        textEncodeNUMERIC_ARRAY((Number[]) value, buff);
        break;
      case UNKNOWN:
        //default to treating unknown as a string
        buff.writeCharSequence(String.valueOf(value), StandardCharsets.UTF_8);
        break;
      default:
        logger.debug("Data type " + id + " does not support text encoding");
        buff.writeCharSequence(String.valueOf(value), StandardCharsets.UTF_8);
        break;
    }
  }

  public static void encodeBinary(DataType id, Object value, ByteBuf buff) {
    switch (id) {
      case BOOL:
        binaryEncodeBOOL((Boolean) value, buff);
        break;
      case BOOL_ARRAY:
        binaryEncodeArray((Boolean[]) value, DataType.BOOL, buff);
        break;
      case INT2:
        binaryEncodeINT2((Number) value, buff);
        break;
      case INT2_ARRAY:
        binaryEncodeArray((Number[]) value, DataType.INT2, buff);
        break;
      case INT4:
        binaryEncodeINT4((Number) value, buff);
        break;
      case INT4_ARRAY:
        binaryEncodeArray((Number[]) value, DataType.INT4, buff);
        break;
      case INT8:
        binaryEncodeINT8((Number) value, buff);
        break;
      case INT8_ARRAY:
        binaryEncodeArray((Number[]) value, DataType.INT8, buff);
        break;
      case FLOAT4:
        binaryEncodeFLOAT4((Number) value, buff);
        break;
      case FLOAT4_ARRAY:
        binaryEncodeArray((Number[]) value, DataType.FLOAT4, buff);
        break;
      case FLOAT8:
        binaryEncodeFLOAT8((Number) value, buff);
        break;
      case FLOAT8_ARRAY:
        binaryEncodeArray((Number[]) value, DataType.FLOAT8, buff);
        break;
      case CHAR:
        binaryEncodeCHAR((String) value, buff);
        break;
      case CHAR_ARRAY:
        binaryEncodeArray((String[]) value, DataType.CHAR, buff);
        break;
      case VARCHAR:
        binaryEncodeVARCHAR((String) value, buff);
        break;
      case VARCHAR_ARRAY:
        binaryEncodeArray((String[]) value, DataType.VARCHAR, buff);
        break;
      case BPCHAR:
        binaryEncodeBPCHAR((String) value, buff);
        break;
      case BPCHAR_ARRAY:
        binaryEncodeArray((String[]) value, DataType.BPCHAR, buff);
        break;
      case TEXT:
        binaryEncodeTEXT((String) value, buff);
        break;
      case TEXT_ARRAY:
        binaryEncodeArray((String[]) value, DataType.TEXT, buff);
        break;
      case NAME:
        binaryEncodeNAME((String) value, buff);
        break;
      case NAME_ARRAY:
        binaryEncodeArray((String[]) value, DataType.NAME, buff);
        break;
      case DATE:
        binaryEncodeDATE((LocalDate) value, buff);
        break;
      case DATE_ARRAY:
        binaryEncodeArray((LocalDate[]) value, DataType.DATE, buff);
        break;
      case TIME:
        binaryEncodeTIME((LocalTime) value, buff);
        break;
      case TIME_ARRAY:
        binaryEncodeArray((LocalTime[]) value, DataType.TIME, buff);
        break;
      case TIMETZ:
        binaryEncodeTIMETZ((OffsetTime) value, buff);
        break;
      case TIMETZ_ARRAY:
        binaryEncodeArray((OffsetTime[]) value, DataType.TIMETZ, buff);
        break;
      case TIMESTAMP:
        binaryEncodeTIMESTAMP((LocalDateTime) value, buff);
        break;
      case TIMESTAMP_ARRAY:
        binaryEncodeArray((LocalDateTime[]) value, DataType.TIMESTAMP, buff);
        break;
      case TIMESTAMPTZ:
        binaryEncodeTIMESTAMPTZ((OffsetDateTime) value, buff);
        break;
      case TIMESTAMPTZ_ARRAY:
        binaryEncodeArray((OffsetDateTime[]) value, DataType.TIMESTAMPTZ, buff);
        break;
      case BYTEA:
        binaryEncodeBYTEA((Buffer) value, buff);
        break;
      case BYTEA_ARRAY:
        binaryEncodeArray((Buffer[]) value, DataType.BYTEA, buff);
        break;
      case UUID:
        binaryEncodeUUID((UUID) value, buff);
        break;
      case UUID_ARRAY:
        binaryEncodeArray((UUID[]) value, DataType.UUID, buff);
        break;
      case JSON:
        binaryEncodeJSON((Object) value, buff);
        break;
      case JSON_ARRAY:
        binaryEncodeArray((Object[]) value, DataType.JSON, buff);
        break;
      case JSONB:
        binaryEncodeJSONB((Object) value, buff);
        break;
      case JSONB_ARRAY:
        binaryEncodeArray((Object[]) value, DataType.JSONB, buff);
        break;
      case POINT:
        binaryEncodePoint((Point) value, buff);
        break;
      case POINT_ARRAY:
        binaryEncodeArray((Point[]) value, DataType.POINT, buff);
        break;
      case LINE:
        binaryEncodeLine((Line) value, buff);
        break;
      case LINE_ARRAY:
        binaryEncodeArray((Line[]) value, DataType.LINE, buff);
        break;
      case LSEG:
        binaryEncodeLseg((LineSegment) value, buff);
        break;
      case LSEG_ARRAY:
        binaryEncodeArray((LineSegment[]) value, DataType.LSEG, buff);
        break;
      case BOX:
        binaryEncodeBox((Box) value, buff);
        break;
      case BOX_ARRAY:
        binaryEncodeArray((Box[]) value, DataType.BOX, buff);
        break;
      case PATH:
        binaryEncodePath((Path) value, buff);
        break;
      case PATH_ARRAY:
        binaryEncodeArray((Path[]) value, DataType.PATH, buff);
        break;
      case POLYGON:
        binaryEncodePolygon((Polygon) value, buff);
        break;
      case POLYGON_ARRAY:
        binaryEncodeArray((Polygon[]) value, DataType.POLYGON, buff);
        break;
      case CIRCLE:
        binaryEncodeCircle((Circle) value, buff);
        break;
      case CIRCLE_ARRAY:
        binaryEncodeArray((Circle[]) value, DataType.CIRCLE, buff);
        break;
      case INTERVAL:
        binaryEncodeINTERVAL((Interval) value, buff);
        break;
      case INTERVAL_ARRAY:
        binaryEncodeArray((Interval[]) value, DataType.INTERVAL, buff);
        break;
      case TS_QUERY:
        binaryEncodeTsQuery((String) value, buff);
        break;
      case TS_QUERY_ARRAY:
        binaryEncodeArray((String[]) value, DataType.TS_QUERY, buff);
        break;
      case TS_VECTOR:
        binaryEncodeTsVector((String) value, buff);
        break;
      case TS_VECTOR_ARRAY:
        binaryEncodeArray((String[]) value, DataType.TS_VECTOR, buff);
        break;
      default:
        logger.debug("Data type " + id + " does not support binary encoding");
        defaultEncodeBinary(value, buff);
        break;
    }
  }

  public static Object decodeBinary(DataType id, int index, int len, ByteBuf buff) {
    switch (id) {
      case BOOL:
        return binaryDecodeBOOL(index, len, buff);
      case BOOL_ARRAY:
        return binaryDecodeArray(BOOLEAN_ARRAY_FACTORY, DataType.BOOL, index, len, buff);
      case INT2:
        return binaryDecodeINT2(index, len, buff);
      case INT2_ARRAY:
        return binaryDecodeArray(SHORT_ARRAY_FACTORY, DataType.INT2, index, len, buff);
      case INT4:
        return binaryDecodeINT4(index, len, buff);
      case INT4_ARRAY:
        return binaryDecodeArray(INTEGER_ARRAY_FACTORY, DataType.INT4, index, len, buff);
      case INT8:
        return binaryDecodeINT8(index, len, buff);
      case INT8_ARRAY:
        return binaryDecodeArray(LONG_ARRAY_FACTORY, DataType.INT8, index, len, buff);
      case FLOAT4:
        return binaryDecodeFLOAT4(index, len, buff);
      case FLOAT4_ARRAY:
        return binaryDecodeArray(FLOAT_ARRAY_FACTORY, DataType.FLOAT4, index, len, buff);
      case FLOAT8:
        return binaryDecodeFLOAT8(index, len, buff);
      case FLOAT8_ARRAY:
        return binaryDecodeArray(DOUBLE_ARRAY_FACTORY, DataType.FLOAT8, index, len, buff);
      case CHAR:
        return binaryDecodeCHAR(index, len, buff);
      case CHAR_ARRAY:
        return binaryDecodeArray(STRING_ARRAY_FACTORY, DataType.CHAR, index, len, buff);
      case VARCHAR:
        return binaryDecodeVARCHAR(index, len, buff);
      case VARCHAR_ARRAY:
        return binaryDecodeArray(STRING_ARRAY_FACTORY, DataType.VARCHAR, index, len, buff);
      case BPCHAR:
        return binaryDecodeBPCHAR(index, len, buff);
      case BPCHAR_ARRAY:
        return binaryDecodeArray(STRING_ARRAY_FACTORY, DataType.BPCHAR, index, len, buff);
      case TEXT:
        return binaryDecodeTEXT(index, len, buff);
      case TEXT_ARRAY:
        return binaryDecodeArray(STRING_ARRAY_FACTORY, DataType.TEXT, index, len, buff);
      case NAME:
        return binaryDecodeNAME(index, len, buff);
      case NAME_ARRAY:
        return binaryDecodeArray(STRING_ARRAY_FACTORY, DataType.NAME, index, len, buff);
      case DATE:
        return binaryDecodeDATE(index, len, buff);
      case DATE_ARRAY:
        return binaryDecodeArray(LOCALDATE_ARRAY_FACTORY, DataType.DATE, index, len, buff);
      case TIME:
        return binaryDecodeTIME(index, len, buff);
      case TIME_ARRAY:
        return binaryDecodeArray(LOCALTIME_ARRAY_FACTORY, DataType.TIME, index, len, buff);
      case TIMETZ:
        return binaryDecodeTIMETZ(index, len, buff);
      case TIMETZ_ARRAY:
        return binaryDecodeArray(OFFSETTIME_ARRAY_FACTORY, DataType.TIMETZ, index, len, buff);
      case TIMESTAMP:
        return binaryDecodeTIMESTAMP(index, len, buff);
      case TIMESTAMP_ARRAY:
        return binaryDecodeArray(LOCALDATETIME_ARRAY_FACTORY, DataType.TIMESTAMP, index, len, buff);
      case TIMESTAMPTZ:
        return binaryDecodeTIMESTAMPTZ(index, len, buff);
      case TIMESTAMPTZ_ARRAY:
        return binaryDecodeArray(OFFSETDATETIME_ARRAY_FACTORY, DataType.TIMESTAMPTZ, index, len, buff);
      case BYTEA:
        return binaryDecodeBYTEA(index, len, buff);
      case BYTEA_ARRAY:
        return binaryDecodeArray(BUFFER_ARRAY_FACTORY, DataType.BYTEA, index, len, buff);
      case UUID:
        return binaryDecodeUUID(index, len, buff);
      case UUID_ARRAY:
        return binaryDecodeArray(UUID_ARRAY_FACTORY, DataType.UUID, index, len, buff);
      case JSON:
        return binaryDecodeJSON(index, len, buff);
      case JSON_ARRAY:
        return binaryDecodeArray(JSON_ARRAY_FACTORY, DataType.JSON, index, len, buff);
      case JSONB:
        return binaryDecodeJSONB(index, len, buff);
      case JSONB_ARRAY:
        return binaryDecodeArray(JSON_ARRAY_FACTORY, DataType.JSONB, index, len, buff);
      case POINT:
        return binaryDecodePoint(index, len, buff);
      case POINT_ARRAY:
        return binaryDecodeArray(POINT_ARRAY_FACTORY, DataType.POINT, index, len, buff);
      case LINE:
        return binaryDecodeLine(index, len, buff);
      case LINE_ARRAY:
        return binaryDecodeArray(LINE_ARRAY_FACTORY, DataType.LINE, index, len, buff);
      case LSEG:
        return binaryDecodeLseg(index, len, buff);
      case LSEG_ARRAY:
        return binaryDecodeArray(LSEG_ARRAY_FACTORY, DataType.LSEG, index, len, buff);
      case BOX:
        return binaryDecodeBox(index, len, buff);
      case BOX_ARRAY:
        return binaryDecodeArray(BOX_ARRAY_FACTORY, DataType.BOX, index, len, buff);
      case PATH:
        return binaryDecodePath(index, len, buff);
      case PATH_ARRAY:
        return binaryDecodeArray(PATH_ARRAY_FACTORY, DataType.PATH, index, len, buff);
      case POLYGON:
        return binaryDecodePolygon(index, len, buff);
      case POLYGON_ARRAY:
        return binaryDecodeArray(POLYGON_ARRAY_FACTORY, DataType.POLYGON, index, len, buff);
      case CIRCLE:
        return binaryDecodeCircle(index, len, buff);
      case CIRCLE_ARRAY:
        return binaryDecodeArray(CIRCLE_ARRAY_FACTORY, DataType.CIRCLE, index, len, buff);
      case INTERVAL:
        return binaryDecodeINTERVAL(index, len, buff);
      case INTERVAL_ARRAY:
        return binaryDecodeArray(INTERVAL_ARRAY_FACTORY, DataType.INTERVAL, index, len, buff);
      case TS_QUERY:
        return binaryDecodeTsQuery(index, len, buff);
      case TS_QUERY_ARRAY:
        return binaryDecodeArray(STRING_ARRAY_FACTORY, DataType.TS_QUERY, index, len, buff);
      case TS_VECTOR:
        return binaryDecodeTsVector(index, len, buff);
      case TS_VECTOR_ARRAY:
        return binaryDecodeArray(STRING_ARRAY_FACTORY, DataType.TS_VECTOR, index, len, buff);
      default:
        logger.debug("Data type " + id + " does not support binary decoding");
        return defaultDecodeBinary(index, len, buff);
    }
  }

  public static Object decodeText(DataType id, int index, int len, ByteBuf buff) {
    switch (id) {
      case BOOL:
        return textDecodeBOOL(index, len, buff);
      case BOOL_ARRAY:
        return textDecodeArray(BOOLEAN_ARRAY_FACTORY, DataType.BOOL, index, len, buff);
      case INT2:
        return textDecodeINT2(index, len, buff);
      case INT2_ARRAY:
        return textDecodeArray(SHORT_ARRAY_FACTORY, DataType.INT2, index, len, buff);
      case INT4:
        return textDecodeINT4(index, len, buff);
      case INT4_ARRAY:
        return textDecodeArray(INTEGER_ARRAY_FACTORY, DataType.INT4, index, len, buff);
      case INT8:
        return textDecodeINT8(index, len, buff);
      case INT8_ARRAY:
        return textDecodeArray(LONG_ARRAY_FACTORY, DataType.INT8, index, len, buff);
      case FLOAT4:
        return textDecodeFLOAT4(index, len, buff);
      case FLOAT4_ARRAY:
        return textDecodeArray(FLOAT_ARRAY_FACTORY, DataType.FLOAT4, index, len, buff);
      case FLOAT8:
        return textDecodeFLOAT8(index, len, buff);
      case FLOAT8_ARRAY:
        return textDecodeArray(DOUBLE_ARRAY_FACTORY, DataType.FLOAT8, index, len, buff);
      case CHAR:
        return textDecodeCHAR(index, len, buff);
      // case CHAR_ARRAY:
      //   return textDecodeCHAR_ARRAY(len, buff);
      case VARCHAR:
        return textDecodeVARCHAR(index, len, buff);
      case VARCHAR_ARRAY:
        return textDecodeArray(STRING_ARRAY_FACTORY, DataType.VARCHAR, index, len, buff);
      case BPCHAR:
        return textDecodeBPCHAR(index, len, buff);
      case BPCHAR_ARRAY:
        return textDecodeArray(STRING_ARRAY_FACTORY, DataType.BPCHAR, index, len, buff);
      case TEXT:
        return textdecodeTEXT(index, len, buff);
      case TEXT_ARRAY:
        return textDecodeArray(STRING_ARRAY_FACTORY, DataType.TEXT, index, len, buff);
      case NAME:
        return textDecodeNAME(index, len, buff);
      case NAME_ARRAY:
        return textDecodeArray(STRING_ARRAY_FACTORY, DataType.NAME, index, len, buff);
      case DATE:
        return textDecodeDATE(index, len, buff);
      case DATE_ARRAY:
        return textDecodeArray(LOCALDATE_ARRAY_FACTORY, DataType.DATE, index, len, buff);
      case TIME:
        return textDecodeTIME(index, len, buff);
      case TIME_ARRAY:
        return textDecodeArray(LOCALTIME_ARRAY_FACTORY, DataType.TIME, index, len, buff);
      case TIMETZ:
        return textDecodeTIMETZ(index, len, buff);
      case TIMETZ_ARRAY:
        return textDecodeArray(OFFSETTIME_ARRAY_FACTORY, DataType.TIMETZ, index, len, buff);
      case TIMESTAMP:
        return textDecodeTIMESTAMP(index, len, buff);
      case TIMESTAMP_ARRAY:
        return textDecodeArray(LOCALDATETIME_ARRAY_FACTORY, DataType.TIMESTAMP, index, len, buff);
      case TIMESTAMPTZ:
        return textDecodeTIMESTAMPTZ(index, len, buff);
      case TIMESTAMPTZ_ARRAY:
        return textDecodeArray(OFFSETDATETIME_ARRAY_FACTORY, DataType.TIMESTAMPTZ, index, len, buff);
      case BYTEA:
        return textDecodeBYTEA(index, len, buff);
      case BYTEA_ARRAY:
        return textDecodeArray(BUFFER_ARRAY_FACTORY, DataType.BYTEA, index, len, buff);
      case UUID:
        return textDecodeUUID(index, len, buff);
      case UUID_ARRAY:
        return textDecodeArray(UUID_ARRAY_FACTORY, DataType.UUID, index, len, buff);
      case NUMERIC:
        return textDecodeNUMERIC(index, len, buff);
      case NUMERIC_ARRAY:
        return textDecodeArray(NUMERIC_ARRAY_FACTORY, DataType.NUMERIC, index, len, buff);
      case JSON:
        return textDecodeJSON(index, len, buff);
      case JSON_ARRAY:
        return textDecodeArray(JSON_ARRAY_FACTORY, DataType.JSON, index, len, buff);
      case JSONB:
        return textDecodeJSONB(index, len, buff);
      case JSONB_ARRAY:
        return textDecodeArray(JSON_ARRAY_FACTORY, DataType.JSONB, index, len, buff);
      case POINT:
        return textDecodePOINT(index, len, buff);
      case POINT_ARRAY:
        return textDecodeArray(POINT_ARRAY_FACTORY, DataType.POINT, index, len, buff);
      case LINE:
        return textDecodeLine(index, len, buff);
      case LINE_ARRAY:
        return textDecodeArray(LINE_ARRAY_FACTORY, DataType.LINE, index, len, buff);
      case LSEG:
        return textDecodeLseg(index, len, buff);
      case LSEG_ARRAY:
        return textDecodeArray(LSEG_ARRAY_FACTORY, DataType.LSEG, index, len, buff);
      case BOX:
        return textDecodeBox(index, len, buff);
      case BOX_ARRAY:
        return textDecodeBoxArray(BOX_ARRAY_FACTORY, index, len, buff);
      case PATH:
        return textDecodePath(index, len, buff);
      case PATH_ARRAY:
        return textDecodeArray(PATH_ARRAY_FACTORY, DataType.PATH, index, len, buff);
      case POLYGON:
        return textDecodePolygon(index, len, buff);
      case POLYGON_ARRAY:
        return textDecodeArray(POLYGON_ARRAY_FACTORY, DataType.POLYGON, index, len, buff);
      case CIRCLE:
        return textDecodeCircle(index, len, buff);
      case CIRCLE_ARRAY:
        return textDecodeArray(CIRCLE_ARRAY_FACTORY, DataType.CIRCLE, index, len, buff);
      case INTERVAL:
        return textDecodeINTERVAL(index, len, buff);
      case INTERVAL_ARRAY:
        return textDecodeArray(INTERVAL_ARRAY_FACTORY, DataType.INTERVAL, index, len, buff);
      case TS_QUERY:
        return textDecodeTsQuery(index, len, buff);
      case TS_QUERY_ARRAY:
        return textDecodeArray(STRING_ARRAY_FACTORY, DataType.TS_QUERY, index, len, buff);
      case TS_VECTOR:
        return textDecodeTsVector(index, len, buff);
      case TS_VECTOR_ARRAY:
        return textDecodeArray(STRING_ARRAY_FACTORY, DataType.TS_VECTOR, index, len, buff);
      default:
        return defaultDecodeText(index, len, buff);
    }
  }


  public static Object prepare(DataType type, Object value) {
    switch (type) {
      case JSON:
      case JSONB:
        if (value == null ||
            value == Tuple.JSON_NULL ||
            value instanceof String ||
            value instanceof Boolean ||
            value instanceof Number ||
            value instanceof JsonObject ||
            value instanceof JsonArray) {
          return value;
        } else {
          return REFUSED_SENTINEL;
        }
      case UNKNOWN:
        if (value instanceof String[]) {
          return Arrays.stream((String[]) value).collect(Collectors.joining(",", "{", "}"));
        } else if (value == null || value instanceof String) {
          return value;
        } else {
          return REFUSED_SENTINEL;
        }
      default:
        Class<?> javaType = type.decodingType;
        return value == null || javaType.isInstance(value) ? value : REFUSED_SENTINEL;
    }
  }

  private static Object defaultDecodeText(int index, int len, ByteBuf buff) {
    // decode unknown text values as text or as an array if it begins with `{`
    if (len > 1 && buff.getByte(index) == '{') {
      return textDecodeArray(STRING_ARRAY_FACTORY, DataType.TEXT, index, len, buff);
    }
    return textdecodeTEXT(index, len, buff);
  }

  private static void defaultEncodeBinary(Object value, ByteBuf buff) {
    // Default to null
    buff.writeInt(-1);
  }

  private static Object defaultDecodeBinary(int index, int len, ByteBuf buff) {
    // Default to null
    return null;
  }

  private static void binaryEncodeBOOL(Boolean value, ByteBuf buff) {
    buff.writeBoolean(value);
  }

  private static Boolean binaryDecodeBOOL(int index, int len, ByteBuf buff) {
    return buff.getBoolean(index);
  }

  private static Boolean textDecodeBOOL(int index, int len, ByteBuf buff) {
    if(buff.getByte(index) == 't') {
      return Boolean.TRUE;
    } else {
      return Boolean.FALSE;
    }
  }

  private static Short textDecodeINT2(int index, int len, ByteBuf buff) {
    return (short) DataTypeCodec.decodeDecStringToLong(index, len, buff);
  }

  private static Short binaryDecodeINT2(int index, int len, ByteBuf buff) {
    return buff.getShort(index);
  }

  private static void binaryEncodeINT2(Number value, ByteBuf buff) {
    buff.writeShort(value.shortValue());
  }

  private static Integer textDecodeINT4(int index, int len, ByteBuf buff) {
    return (int) decodeDecStringToLong(index, len, buff);
  }

  private static Integer binaryDecodeINT4(int index, int len, ByteBuf buff) {
    return buff.getInt(index);
  }

  private static void binaryEncodeINT4(Number value, ByteBuf buff) {
    buff.writeInt(value.intValue());
  }

  private static Long textDecodeINT8(int index, int len, ByteBuf buff) {
    return decodeDecStringToLong(index, len, buff);
  }

  private static Long binaryDecodeINT8(int index, int len, ByteBuf buff) {
    return buff.getLong(index);
  }

  private static void binaryEncodeINT8(Number value, ByteBuf buff) {
    buff.writeLong(value.longValue());
  }

  private static Float textDecodeFLOAT4(int index, int len, ByteBuf buff) {
    // Todo optimize that
    CharSequence cs = buff.getCharSequence(index, len, StandardCharsets.UTF_8);
    return Float.parseFloat(cs.toString());
  }

  private static Float binaryDecodeFLOAT4(int index, int len, ByteBuf buff) {
    return buff.getFloat(index);
  }

  private static void binaryEncodeFLOAT4(Number value, ByteBuf buff) {
    buff.writeFloat(value.floatValue());
  }

  private static void binaryEncodeFLOAT8(Number value, ByteBuf buff) {
    buff.writeDouble(value.doubleValue());
  }

  private static Double binaryDecodeFLOAT8(int index, int len, ByteBuf buff) {
    return buff.getDouble(index);
  }

  private static double textDecodeFLOAT8(int index, int len, ByteBuf buff) {
    // Todo optimize that
    CharSequence cs = buff.getCharSequence(index, len, StandardCharsets.UTF_8);
    return Double.parseDouble(cs.toString());
  }

  private static Number textDecodeNUMERIC(int index, int len, ByteBuf buff) {
    // Todo optimize that
    CharSequence cs = buff.getCharSequence(index, len, StandardCharsets.UTF_8);
    return Numeric.parse(cs.toString());
  }

  private static Point textDecodePOINT(int index, int len, ByteBuf buff) {
    // Point representation: (x,y)
    int idx = ++index;
    int s = buff.indexOf(idx, idx + len, (byte) ',');
    int t = s - idx;
    double x = textDecodeFLOAT8(idx, t, buff);
    double y = textDecodeFLOAT8(s + 1, len - t - 3, buff);
    return new Point(x, y);
  }

  private static Line textDecodeLine(int index, int len, ByteBuf buff) {
    // Line representation: {a,b,c}
    int idxOfFirstSeparator = buff.indexOf(index, index + len, (byte) ',');
    int idxOfLastSeparator = buff.indexOf(index + len, index, (byte) ',');

    int idx = index + 1;
    double a = textDecodeFLOAT8(idx, idxOfFirstSeparator - idx, buff);
    double b = textDecodeFLOAT8(idxOfFirstSeparator + 1, idxOfLastSeparator - idxOfFirstSeparator - 1, buff);
    double c = textDecodeFLOAT8(idxOfLastSeparator + 1, index + len - idxOfLastSeparator - 2, buff);
    return new Line(a, b, c);
  }

  private static LineSegment textDecodeLseg(int index, int len, ByteBuf buff) {
    // Lseg representation: [p1,p2]
    int idxOfPointsSeparator = buff.indexOf(index, index+len, (byte) ')') + 1;
    int lenOfP1 = idxOfPointsSeparator - index - 1;
    Point p1 = textDecodePOINT(index + 1, lenOfP1, buff);
    Point p2 = textDecodePOINT(idxOfPointsSeparator + 1, len - lenOfP1 - 3, buff);
    return new LineSegment(p1, p2);
  }

  private static Box textDecodeBox(int index, int len, ByteBuf buff) {
    // Box representation: p1,p2
    int idxOfPointsSeparator = buff.indexOf(index, index+len, (byte) ')') + 1;
    int lenOfUpperRightCornerPoint = idxOfPointsSeparator - index;
    Point upperRightCorner = textDecodePOINT(index, lenOfUpperRightCornerPoint, buff);
    Point lowerLeftCorner = textDecodePOINT(idxOfPointsSeparator + 1, len - lenOfUpperRightCornerPoint - 1, buff);
    return new Box(upperRightCorner, lowerLeftCorner);
  }

  private static Box[] textDecodeBoxArray(IntFunction<Box[]> supplier, int index, int len, ByteBuf buff) {
    // Box Array representation: {box1;box2;...boxN}
    List<Box> boxes = new ArrayList<>();
    int start = index + 1;
    int end = index + len - 1;
    while (start < end) {
      int idxOfBoxSeparator = buff.indexOf(start, end + 1, (byte) ';');
      if (idxOfBoxSeparator == -1) {
        // the last box
        Box box = textDecodeBox(start, end - start, buff);
        boxes.add(box);
        break;
      }
      int lenOfBox = idxOfBoxSeparator - start;
      Box box = textDecodeBox(start, lenOfBox, buff);
      boxes.add(box);
      start = idxOfBoxSeparator + 1;
    }
    return boxes.toArray(supplier.apply(boxes.size()));
  }

  private static Path textDecodePath(int index, int len, ByteBuf buff) {
    // Path representation: (p1,p2...pn) or [p1,p2...pn]
    byte first = buff.getByte(index);
    byte last = buff.getByte(index + len - 1);
    boolean isOpen;
    if (first == '(' && last == ')') {
      isOpen = false;
    } else if (first == '[' && last == ']') {
      isOpen = true;
    } else {
      throw new DecoderException("Decoding Path is in wrong syntax");
    }
    List<Point> points = textDecodeMultiplePoints(index + 1, len - 2, buff);
    return new Path(isOpen, points);
  }

  private static Polygon textDecodePolygon(int index, int len, ByteBuf buff) {
    // Polygon representation: (p1,p2...pn)
    List<Point> points = textDecodeMultiplePoints(index + 1, len - 2, buff);
    return new Polygon(points);
  }

  // this might be useful for decoding Lseg, Box, Path, Polygon Data Type.
  private static List<Point> textDecodeMultiplePoints(int index, int len, ByteBuf buff) {
    // representation: p1,p2,p3...pn
    List<Point> points = new ArrayList<>();
    int start = index;
    int end = index + len - 1;
    while (start < end) {
      int rightParenthesis = buff.indexOf(start, end + 1, (byte) ')');
      int idxOfPointSeparator = rightParenthesis + 1;
      int lenOfPoint = idxOfPointSeparator - start;
      Point point = textDecodePOINT(start, lenOfPoint, buff);
      points.add(point);
      start = idxOfPointSeparator + 1;
    }
    return points;
  }

  private static Circle textDecodeCircle(int index, int len, ByteBuf buff) {
    // Circle representation: <p,r>
    int idxOfLastComma = buff.indexOf(index + len - 1, index, (byte) ',');
    int lenOfPoint = idxOfLastComma - index - 1;
    Point center = textDecodePOINT(index + 1, lenOfPoint, buff);
    int lenOfRadius = len - lenOfPoint - 3;
    double radius = textDecodeFLOAT8(idxOfLastComma + 1, lenOfRadius, buff);
    return new Circle(center, radius);
  }

  private static Interval textDecodeINTERVAL(int index, int len, ByteBuf buff) {
    CharSequence cs = buff.getCharSequence(index, len, StandardCharsets.UTF_8);
    String value = cs.toString();
    int years = 0, months = 0, days = 0, hours = 0, minutes = 0, seconds = 0, microseconds = 0;
    final List<String> chunks = new ArrayList<>(7);
    int idx = 0;
    for (;;) {
      int newIdx = value.indexOf(' ', idx);
      if (newIdx == -1) {
        chunks.add(value.substring(idx));
        break;
      }
      chunks.add(value.substring(idx, newIdx));
      idx = newIdx + 1;
    }
    boolean hasTime = chunks.size() % 2 == 1;
    int dateChunkMax = hasTime ? chunks.size() - 1 : chunks.size();
    for (int i = 0; i < dateChunkMax; i += 2) {
      int val = Integer.parseInt(chunks.get(i));
      switch (chunks.get(i + 1)) {
        case "year":
        case "years":
          years = val;
          break;
        case "mon":
        case "mons":
          months = val;
          break;
        case "day":
        case "days":
          days = val;
          break;
      }
    }
    if (hasTime) {
      String timeChunk = chunks.get(chunks.size() - 1);
      boolean isNeg = timeChunk.charAt(0) == '-';
      if (isNeg) timeChunk = timeChunk.substring(1);
      int sidx = 0;
      for (;;) {
        int newIdx = timeChunk.indexOf(':', sidx);
        if (newIdx == -1) {
          int m = timeChunk.substring(sidx).indexOf('.');
          if(m == -1) {
            // seconds without microseconds
            seconds = isNeg ? -Integer.parseInt(timeChunk.substring(sidx))
              : Integer.parseInt(timeChunk.substring(sidx));
          } else {
            // seconds with microseconds
            seconds =  isNeg ? -Integer.parseInt(timeChunk.substring(sidx).substring(0, m))
              : Integer.parseInt(timeChunk.substring(sidx).substring(0, m));
            microseconds = isNeg ? -Integer.parseInt(timeChunk.substring(sidx).substring(m + 1))
              : Integer.parseInt(timeChunk.substring(sidx).substring(m + 1));
          }
          break;
        }
        // hours
        if(sidx == 0) {
          hours = isNeg ? -Integer.parseInt(timeChunk.substring(sidx, newIdx))
            : Integer.parseInt(timeChunk.substring(sidx, newIdx));
        } else {
          // minutes
          minutes = isNeg ? -Integer.parseInt(timeChunk.substring(sidx, newIdx))
            : Integer.parseInt(timeChunk.substring(sidx, newIdx));
        }
        sidx = newIdx + 1;
      }
    }
    return new Interval(years, months, days, hours, minutes, seconds, microseconds);
  }

  private static void textEncodeNUMERIC(Number value, ByteBuf buff) {
    String s = value.toString();
    buff.writeCharSequence(s, StandardCharsets.UTF_8);
  }

  private static void textEncodeNUMERIC_ARRAY(Number[] value, ByteBuf buff) {
    textEncodeArray(value, DataType.NUMERIC, buff);
  }

  private static void binaryEncodeCHAR(String value, ByteBuf buff) {
    binaryEncodeTEXT(value, buff);
  }

  private static String textDecodeCHAR(int index, int len, ByteBuf buff) {
    return buff.getCharSequence(index, len, StandardCharsets.UTF_8).toString();
  }

  private static String binaryDecodeCHAR(int index, int len, ByteBuf buff) {
    return binaryDecodeTEXT(index, len, buff);
  }

  private static void binaryEncodeVARCHAR(String value, ByteBuf buff) {
    String s = String.valueOf(value);
    buff.writeCharSequence(s, StandardCharsets.UTF_8);
  }

  private static String textDecodeVARCHAR(int index, int len, ByteBuf buff) {
    return buff.getCharSequence(index, len, StandardCharsets.UTF_8).toString();
  }

  private static String binaryDecodeVARCHAR(int index, int len, ByteBuf buff) {
    return buff.getCharSequence(index, len, StandardCharsets.UTF_8).toString();
  }

  private static String textDecodeBPCHAR(int index, int len, ByteBuf buff) {
    return buff.getCharSequence(index, len, StandardCharsets.UTF_8).toString();
  }

  private static void binaryEncodeBPCHAR(String value, ByteBuf buff) {
    buff.writeCharSequence(value, StandardCharsets.UTF_8);
  }

  private static String binaryDecodeBPCHAR(int index, int len, ByteBuf buff) {
    return buff.getCharSequence(index, len, StandardCharsets.UTF_8).toString();
  }

  private static String textdecodeTEXT(int index, int len, ByteBuf buff) {
    return buff.getCharSequence(index, len, StandardCharsets.UTF_8).toString();
  }

  private static void binaryEncodeTEXT(String value, ByteBuf buff) {
    String s = String.valueOf(value);
    buff.writeCharSequence(s, StandardCharsets.UTF_8);
  }

  private static String binaryDecodeTEXT(int index, int len, ByteBuf buff) {
    return buff.getCharSequence(index, len, StandardCharsets.UTF_8).toString();
  }

  private static String textDecodeNAME(int index, int len, ByteBuf buff) {
    return buff.getCharSequence(index, len, StandardCharsets.UTF_8).toString();
  }


  private static void binaryEncodeNAME(String value, ByteBuf buff) {
    String s = String.valueOf(value);
    buff.writeCharSequence(s, StandardCharsets.UTF_8);
  }

  private static String binaryDecodeNAME(int index, int len, ByteBuf buff) {
    return buff.getCharSequence(index, len, StandardCharsets.UTF_8).toString();
  }

  private static void binaryEncodeDATE(LocalDate value, ByteBuf buff) {
    buff.writeInt((int) -value.until(LOCAL_DATE_EPOCH, ChronoUnit.DAYS));
  }

  private static LocalDate binaryDecodeDATE(int index, int len, ByteBuf buff) {
    return LOCAL_DATE_EPOCH.plus(buff.getInt(index), ChronoUnit.DAYS);
  }

  private static LocalDate textDecodeDATE(int index, int len, ByteBuf buff) {
    CharSequence cs = buff.getCharSequence(index, len, StandardCharsets.UTF_8);
    return LocalDate.parse(cs);
  }

  private static void binaryEncodeTIME(LocalTime value, ByteBuf buff) {
    buff.writeLong(value.getLong(ChronoField.MICRO_OF_DAY));
  }

  private static LocalTime binaryDecodeTIME(int index, int len, ByteBuf buff) {
    // micros to nanos
    return LocalTime.ofNanoOfDay(buff.getLong(index) * 1000);
  }

  private static LocalTime textDecodeTIME(int index, int len, ByteBuf buff) {
    CharSequence cs = buff.getCharSequence(index, len, StandardCharsets.UTF_8);
    return LocalTime.parse(cs);
  }

  private static void binaryEncodeTIMETZ(OffsetTime value, ByteBuf buff) {
    buff.writeLong(value.toLocalTime().getLong(ChronoField.MICRO_OF_DAY));
    // zone offset in seconds (should we change it to UTC ?)
    buff.writeInt(-value.getOffset().getTotalSeconds());
  }

  private static OffsetTime binaryDecodeTIMETZ(int index, int len, ByteBuf buff) {
    // micros to nanos
    return OffsetTime.of(LocalTime.ofNanoOfDay(buff.getLong(index) * 1000),
      // zone offset in seconds (should we change it to UTC ?)
      ZoneOffset.ofTotalSeconds(-buff.getInt(index + 8)));
  }

  private static OffsetTime textDecodeTIMETZ(int index, int len, ByteBuf buff) {
    CharSequence cs = buff.getCharSequence(index, len, StandardCharsets.UTF_8);
    return OffsetTime.parse(cs, TIMETZ_FORMAT);
  }

  private static void binaryEncodeTIMESTAMP(LocalDateTime value, ByteBuf buff) {
    buff.writeLong(-value.until(LOCAL_DATE_TIME_EPOCH, ChronoUnit.MICROS));
  }

  private static LocalDateTime binaryDecodeTIMESTAMP(int index, int len, ByteBuf buff) {
    return LOCAL_DATE_TIME_EPOCH.plus(buff.getLong(index), ChronoUnit.MICROS);
  }

  private static LocalDateTime textDecodeTIMESTAMP(int index, int len, ByteBuf buff) {
    CharSequence cs = buff.getCharSequence(index, len, StandardCharsets.UTF_8);
    return LocalDateTime.parse(cs, TIMESTAMP_FORMAT);
  }

  private static OffsetDateTime binaryDecodeTIMESTAMPTZ(int index, int len, ByteBuf buff) {
    return OFFSET_DATE_TIME_EPOCH.plus(buff.getLong(index), ChronoUnit.MICROS);
  }

  private static void binaryEncodeTIMESTAMPTZ(OffsetDateTime value, ByteBuf buff) {
    buff.writeLong(-value.until(OFFSET_DATE_TIME_EPOCH, ChronoUnit.MICROS));
  }

  private static OffsetDateTime textDecodeTIMESTAMPTZ(int index, int len, ByteBuf buff) {
    CharSequence cs = buff.getCharSequence(index, len, StandardCharsets.UTF_8);
    return OffsetDateTime.parse(cs, TIMESTAMPTZ_FORMAT);
  }

  private static Buffer textDecodeBYTEA(int index, int len, ByteBuf buff) {
    if (isHexFormat(index, len, buff)) {
      // hex format
      // Shift 2 bytes: skip \x prolog
      return decodeHexStringToBytes(index + 2, len - 2, buff);
    } else {
      // escape format
      return decodeEscapeByteaStringToBuffer(index, len, buff);
    }
  }

  private static void binaryEncodeBYTEA(Buffer value, ByteBuf buff) {
    ByteBuf byteBuf = value.getByteBuf();
    buff.writeBytes(byteBuf);
  }

  private static Buffer binaryDecodeBYTEA(int index, int len, ByteBuf buff) {
    Buffer target = Buffer.buffer(len);
    target.appendBuffer(Buffer.buffer(buff.slice(index, len)));
    return target;
  }

  private static void binaryEncodeUUID(UUID uuid, ByteBuf buff) {
    buff.writeLong(uuid.getMostSignificantBits());
    buff.writeLong(uuid.getLeastSignificantBits());
  }

  private static void binaryEncodePoint(Point point, ByteBuf buff) {
    binaryEncodeFLOAT8(point.x, buff);
    binaryEncodeFLOAT8(point.y, buff);
  }

  private static Point binaryDecodePoint(int index, int len, ByteBuf buff) {
    double x = binaryDecodeFLOAT8(index, 8, buff);
    double y = binaryDecodeFLOAT8(index + 8, 8, buff);
    return new Point(x, y);
  }

  private static void binaryEncodeLine(Line line, ByteBuf buff) {
    binaryEncodeFLOAT8(line.getA(), buff);
    binaryEncodeFLOAT8(line.getB(), buff);
    binaryEncodeFLOAT8(line.getC(), buff);
  }

  private static Line binaryDecodeLine(int index, int len, ByteBuf buff) {
    double a = binaryDecodeFLOAT8(index, 8, buff);
    double b = binaryDecodeFLOAT8(index + 8, 8, buff);
    double c = binaryDecodeFLOAT8(index + 16, 8, buff);
    return new Line(a, b, c);
  }

  private static void binaryEncodeLseg(LineSegment lseg, ByteBuf buff) {
    binaryEncodePoint(lseg.getP1(), buff);
    binaryEncodePoint(lseg.getP2(), buff);
  }

  private static LineSegment binaryDecodeLseg(int index, int len, ByteBuf buff) {
    Point p1 = binaryDecodePoint(index, 16, buff);
    Point p2 = binaryDecodePoint(index + 16, 16, buff);
    return new LineSegment(p1, p2);
  }

  private static void binaryEncodeBox(Box box, ByteBuf buff) {
    binaryEncodePoint(box.getUpperRightCorner(), buff);
    binaryEncodePoint(box.getLowerLeftCorner(), buff);
  }

  private static Box binaryDecodeBox(int index, int len, ByteBuf buff) {
    Point upperRightCorner = binaryDecodePoint(index, 16, buff);
    Point lowerLeftCorner = binaryDecodePoint(index + 16, 16, buff);
    return new Box(upperRightCorner, lowerLeftCorner);
  }

  private static void binaryEncodePath(Path path, ByteBuf buff) {
    if (path.isOpen()) {
      buff.writeByte(0);
    } else {
      buff.writeByte(1);
    }
    List<Point> points = path.getPoints();
    binaryEncodeINT4(points.size(), buff);
    for (Point point : points) {
      binaryEncodePoint(point, buff);
    }
  }

  private static Path binaryDecodePath(int index, int len, ByteBuf buff) {
    byte first = buff.getByte(index);
    boolean isOpen;
    if (first == 0) {
      isOpen = true;
    } else if (first == 1) {
      isOpen = false;
    } else {
      throw new DecoderException("Decoding Path exception");
    }
    int idx = ++index;
    int numberOfPoints = binaryDecodeINT4(idx, 4, buff);
    idx += 4;
    List<Point> points = new ArrayList<>();
    // maybe we need some check?
    for (int i = 0; i < numberOfPoints; i++) {
      points.add(binaryDecodePoint(idx, 16, buff));
      idx += 16;
    }
    return new Path(isOpen, points);
  }

  private static void binaryEncodePolygon(Polygon polygon, ByteBuf buff) {
    List<Point> points = polygon.getPoints();
    int numberOfPoints = points.size();
    binaryEncodeINT4(numberOfPoints, buff);
    for (Point point : points) {
      binaryEncodeFLOAT8(point.x, buff);
      binaryEncodeFLOAT8(point.y, buff);
    }
  }

  private static Polygon binaryDecodePolygon(int index, int len, ByteBuf buff) {
    int idx = index;
    int numberOfPoints = binaryDecodeINT4(index, 4, buff);
    idx += 4;
    List<Point> points = new ArrayList<>();
    for (int i = 0; i < numberOfPoints; i++) {
      points.add(binaryDecodePoint(idx, 16, buff));
      idx += 16;
    }
    return new Polygon(points);
  }

  private static void binaryEncodeCircle(Circle circle, ByteBuf buff) {
    binaryEncodePoint(circle.getCenterPoint(), buff);
    binaryEncodeFLOAT8(circle.getRadius(), buff);
  }

  private static Circle binaryDecodeCircle(int index, int len, ByteBuf buff) {
    Point center = binaryDecodePoint(index, 16, buff);
    double radius = binaryDecodeFLOAT8(index + 16, 8, buff);
    return new Circle(center, radius);
  }

  private static void binaryEncodeINTERVAL(Interval interval, ByteBuf buff) {
    Duration duration = Duration
      .ofHours(interval.getHours())
      .plusMinutes(interval.getMinutes())
      .plusSeconds(interval.getSeconds())
      .plus(interval.getMicroseconds(), ChronoUnit.MICROS);
    // days won't be changed
    Period monthYear = Period.of(interval.getYears(), interval.getMonths(), interval.getDays()).normalized();
    binaryEncodeINT8(NANOSECONDS.toMicros(duration.toNanos()), buff);
    binaryEncodeINT4(monthYear.getDays(), buff);
    binaryEncodeINT4((int) monthYear.toTotalMonths(), buff);
  }

  private static Interval binaryDecodeINTERVAL(int index, int len, ByteBuf buff) {
    Duration duration = Duration.of(buff.getLong(index), ChronoUnit.MICROS);
    final long hours = duration.toHours();
    duration = duration.minusHours(hours);
    final long minutes = duration.toMinutes();
    duration = duration.minusMinutes(minutes);
    final long seconds = NANOSECONDS.toSeconds(duration.toNanos());
    duration = duration.minusSeconds(seconds);
    final long microseconds = NANOSECONDS.toMicros(duration.toNanos());
    int days = buff.getInt(index + 8);
    int months = buff.getInt(index + 12);
    Period monthYear = Period.of(0, months, days).normalized();
    return new Interval(monthYear.getYears(), monthYear.getMonths(), monthYear.getDays(),
      (int) hours, (int) minutes, (int) seconds, (int) microseconds);
  }

  private static UUID binaryDecodeUUID(int index, int len, ByteBuf buff) {
    return new UUID(buff.getLong(index), buff.getLong(index + 8));
  }

  private static UUID textDecodeUUID(int index, int len, ByteBuf buff) {
    return java.util.UUID.fromString(buff.getCharSequence(index, len, StandardCharsets.UTF_8).toString());
  }

  private static Object textDecodeJSON(int index, int len, ByteBuf buff) {
    return textDecodeJSONB(index, len, buff);
  }

  private static Object binaryDecodeJSON(int index, int len, ByteBuf buff) {
    return textDecodeJSONB(index, len, buff);
  }

  private static void binaryEncodeJSON(Object value, ByteBuf buff) {
    String s;
    if (value == Tuple.JSON_NULL) {
      s = "null";
    } else {
      s = JsonCodec.INSTANCE.toString(value);
    }
    buff.writeCharSequence(s, StandardCharsets.UTF_8);
  }

  private static Object textDecodeJSONB(int index, int len, ByteBuf buff) {

    // Try to do without the intermediary String (?)
    CharSequence cs = buff.getCharSequence(index, len, StandardCharsets.UTF_8);
    Object value = null;
    String s = cs.toString();
    int pos = 0;
    while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) {
      pos++;
    }
    if (pos == s.length()) {
      return null;
    } else if (s.charAt(pos) == '{') {
      value = new JsonObject(s);
    } else if (s.charAt(pos) == '[') {
      value = new JsonArray(s);
    } else {
      Object o = JsonCodec.INSTANCE.fromString(s, Object.class);
      if (o == null) {
        return Tuple.JSON_NULL;
      }
      if (o instanceof Number || o instanceof Boolean || o instanceof String) {
        return o;
      }
      return null;
    }
    return value;
  }

  private static Object binaryDecodeJSONB(int index, int len, ByteBuf buff) {
    // Skip 1 byte for version (which is 1)
    return textDecodeJSONB(index + 1, len - 1, buff);
  }

  private static void binaryEncodeJSONB(Object value, ByteBuf buff) {
    buff.writeByte(1); // version
    binaryEncodeJSON(value, buff);
  }

  private static String binaryDecodeTsVector(int index, int len, ByteBuf buff) {
    return buff.getCharSequence(index, len, StandardCharsets.UTF_8).toString();
  }

  private static void binaryEncodeTsVector(String value, ByteBuf buff) {
    buff.writeCharSequence(String.valueOf(value), StandardCharsets.UTF_8);
  }

  private static String binaryDecodeTsQuery(int index, int len, ByteBuf buff) {
    return buff.getCharSequence(index, len, StandardCharsets.UTF_8).toString();
  }

  private static void binaryEncodeTsQuery(String value, ByteBuf buff) {
    buff.writeCharSequence(String.valueOf(value), StandardCharsets.UTF_8);
  }

  private static String textDecodeTsVector(int index, int len, ByteBuf buff) {
    return buff.getCharSequence(index, len, StandardCharsets.UTF_8).toString();
  }

  private static String textDecodeTsQuery(int index, int len, ByteBuf buff) {
    return buff.getCharSequence(index, len, StandardCharsets.UTF_8).toString();
  }
  /**
   * Decode the specified {@code buff} formatted as a decimal string starting at the readable index
   * with the specified {@code length} to a long.
   *
   * @param index the hex string index
   * @param len the hex string length
   * @param buff the byte buff to read from
   * @return the decoded value as a long
   */
  private static long decodeDecStringToLong(int index, int len, ByteBuf buff) {
    long value = 0;
    if (len > 0) {
      int to = index + len;
      boolean neg = false;
      if (buff.getByte(index) == '-') {
        neg = true;
        index++;
      }
      while (index < to) {
        byte ch = buff.getByte(index++);
        byte nibble = (byte)(ch - '0');
        value = value * 10 + nibble;
      }
      if (neg) {
        value = -value;
      }
    }
    return value;
  }

  /**
   * Decode the specified {@code buff} formatted as an hex string starting at the buffer readable index
   * with the specified {@code length} to a {@link Buffer}.
   *
   * @param len the hex string length
   * @param buff the byte buff to read from
   * @return the decoded value as a Buffer
   */
  private static Buffer decodeHexStringToBytes(int index, int len, ByteBuf buff) {
    len = len >> 1;
    Buffer buffer = Buffer.buffer(len);
    for (int i = 0; i < len; i++) {
      byte b0 = decodeHexChar(buff.getByte(index++));
      byte b1 = decodeHexChar(buff.getByte(index++));
      buffer.appendByte((byte) (b0 * 16 + b1));
    }
    return buffer;
  }

  private static byte decodeHexChar(byte ch) {
    return (byte)(((ch & 0x1F) + ((ch >> 6) * 0x19) - 0x10) & 0x0F);
  }

  private static boolean isHexFormat(int index, int len, ByteBuf buff) {
    return len >= 2 && buff.getByte(index) == '\\' && buff.getByte(index + 1) == 'x';
  }

  private static Buffer decodeEscapeByteaStringToBuffer(int index, int len, ByteBuf buff) {
    Buffer buffer = Buffer.buffer();

    int pos = 0;
    while (pos < len) {
      byte current = buff.getByte(pos + index);

      if (current == '\\') {
        if (pos + 2 <= len && buff.getByte(pos + index + 1) == '\\') {
          // check double backslashes
          buffer.appendByte((byte) '\\');
          pos += 2;
        } else if (pos + 4 <= len) {
          // a preceded backslash with three-digit octal value
          int high = Character.digit(buff.getByte(pos + index + 1), 8) << 6;
          int medium = Character.digit(buff.getByte(pos + index + 2), 8) << 3;
          int low = Character.digit(buff.getByte(pos + index + 3), 8);
          int escapedValue = high + medium + low;

          buffer.appendByte((byte) escapedValue);
          pos += 4;
        } else {
          throw new DecoderException("Decoding unexpected BYTEA escape format");
        }
      } else {
        // printable octets
        buffer.appendByte(current);
        pos++;
      }
    }

    return buffer;
  }

  private static <T> T[] binaryDecodeArray(IntFunction<T[]> supplier, DataType type, int index, int len, ByteBuf buff) {
    if (len == 12) {
      return supplier.apply(0);
    }
    int dim = buff.getInt(index);    // read ndim
    index += 4;
    index += 4;                      // skip dataoffset
    index += 4;                      // skip elemtype
    int length = buff.getInt(index); // read dimensions
    index += 4;
    index += 4;                      // skip lower bnds
    if (dim != 1) {
      logger.warn("Only arrays of dimension 1 are supported");
      return null;
    }
    T[] array = supplier.apply(length);
    for (int i = 0; i < array.length; i++) {
      int l = buff.getInt(index);
      index += 4;
      if (l != -1) {
        array[i] = (T) decodeBinary(type, index, l, buff);
        index += l;
      }
    }
    return array;
  }

  private static <T> void binaryEncodeArray(T[] values, DataType type, ByteBuf buff){
    int startIndex = buff.writerIndex();
    buff.writeInt(1);             // ndim
    buff.writeInt(0);             // dataoffset
    buff.writeInt(type.id);       // elemtype
    buff.writeInt(values.length); // dimension
    buff.writeInt(1);             // lower bnds
    boolean hasNulls = false;
    for (T value : values) {
      if (value == null) {
        hasNulls = true;
        buff.writeInt(-1);
      } else {
        int idx = buff.writerIndex();
        buff.writeInt(0);
        encodeBinary(type, value, buff);
        buff.setInt(idx, buff.writerIndex() - idx - 4);
      }
    }
    if (hasNulls) {
      buff.setInt(startIndex + 4, 1);
    }
  }

  private static <T> T[] textDecodeArray(IntFunction<T[]> supplier, DataType type, int index, int len, ByteBuf buff) {
    List<T> list = new ArrayList<>();
    int from = index + 1; // Set index after '{'
    int to = index + len - 1; // Set index before '}'
    while (from < to) {
      // Escaped content ?
      boolean escaped = buff.getByte(from) == '"';
      int idx;
      if (escaped) {
        idx = buff.forEachByte(from, to - from, new UTF8StringEndDetector());
        idx = buff.indexOf(idx, to, (byte) ','); // SEE iF WE CAN GET RID oF IT
      } else {
        idx = buff.indexOf(from, to, (byte) ',');
      }
      if (idx == -1) {
        idx = to;
      }
      T elt = textDecodeArrayElement(type, from, idx - from, buff);
      list.add(elt);
      from = idx + 1;
    }
    return list.toArray(supplier.apply(list.size()));
  }

  private static <T> T textDecodeArrayElement(DataType type, int index, int len, ByteBuf buff) {
    if (len == 4
      && Character.toUpperCase(buff.getByte(index)) == 'N'
      && Character.toUpperCase(buff.getByte(index + 1)) == 'U'
      && Character.toUpperCase(buff.getByte(index + 2)) == 'L'
      && Character.toUpperCase(buff.getByte(index + 3)) == 'L'
    ) {
      return null;
    } else {
      boolean escaped = buff.getByte(index) == '"';
      if (escaped) {
        // Some escaping - improve that later...
        String s = buff.toString(index + 1, len - 2, StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        for (int i = 0;i < s.length();i++) {
          char c = s.charAt(i);
          if (c == '\\') {
            c = s.charAt(++i);
          }
          sb.append(c);
        }
        buff = Unpooled.copiedBuffer(sb, StandardCharsets.UTF_8);
        index = 0;
        len = buff.readableBytes();
      }
      return (T) decodeText(type, index, len, buff);
    }
  }

  private static <T> void textEncodeArray(T[] values, DataType type, ByteBuf buff){
    buff.writeByte('{');
    int len = values.length;
    for (int i = 0; i < len; i++) {
      if (i > 0) {
        buff.writeByte(',');
      }
      T value = values[i];
      if (value != null) {
        textEncode(type, value, buff);
      } else {
        buff.writeByte('N');
        buff.writeByte('U');
        buff.writeByte('L');
        buff.writeByte('L');
      }
    }
    buff.writeByte('}');
  }
}

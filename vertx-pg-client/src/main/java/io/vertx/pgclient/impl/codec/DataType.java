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

import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.data.*;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.data.Numeric;
import io.vertx.sqlclient.internal.TupleInternal;

import java.sql.JDBCType;
import java.time.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

/**
 * PostgreSQL <a href="https://github.com/postgres/postgres/blob/master/src/include/catalog/pg_type.h">object
 * identifiers (OIDs)</a> for data types
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
public enum DataType {

  BOOL(16, true, Boolean.class, JDBCType.BOOLEAN, null, Tuple::getBoolean, DataTypeEstimator.BOOL),
  BOOL_ARRAY(1000, true, Boolean[].class, JDBCType.BOOLEAN, null, Tuple::getArrayOfBooleans, DataTypeEstimator.BOOL),
  INT2(21, true, Short.class, Number.class, JDBCType.SMALLINT, Tuple::getShort, null, DataTypeEstimator.INT2),
  INT2_ARRAY(1005, true, Short[].class, Number[].class, JDBCType.SMALLINT, Tuple::getArrayOfShorts, null, DataTypeEstimator.INT2),
  INT4(23, true, Integer.class, Number.class, JDBCType.INTEGER, Tuple::getInteger, null, DataTypeEstimator.INT4),
  INT4_ARRAY(1007, true, Integer[].class, Number[].class, JDBCType.INTEGER, Tuple::getArrayOfIntegers, null, DataTypeEstimator.INT4),
  INT8(20, true, Long.class, Number.class, JDBCType.BIGINT, Tuple::getLong, null, DataTypeEstimator.INT8),
  INT8_ARRAY(1016, true, Long[].class, Number[].class, JDBCType.BIGINT, Tuple::getArrayOfLongs, null, DataTypeEstimator.INT8),
  FLOAT4(700, true, Float.class, Number.class, JDBCType.REAL, Tuple::getFloat, null, DataTypeEstimator.FLOAT4),
  FLOAT4_ARRAY(1021, true, Float[].class, Number[].class, JDBCType.REAL, Tuple::getArrayOfFloats, null, DataTypeEstimator.FLOAT4),
  FLOAT8(701, true, Double.class, Number.class, JDBCType.DOUBLE, Tuple::getDouble, null, DataTypeEstimator.FLOAT8),
  FLOAT8_ARRAY(1022, true, Double[].class, Number[].class, JDBCType.DOUBLE, Tuple::getArrayOfDoubles, null, DataTypeEstimator.FLOAT8),
  NUMERIC(1700, false, Numeric.class, Number.class, JDBCType.NUMERIC, Tuple::getNumeric, ParamExtractor::prepareNumeric, DataTypeEstimator.NUMERIC),
  NUMERIC_ARRAY(1231, false, Numeric[].class, Number[].class, JDBCType.NUMERIC, Tuple::getArrayOfNumerics, ParamExtractor::prepareNumeric, DataTypeEstimator.NUMERIC_ARRAY),
  MONEY(790, true, Money.class, null, DataTypeEstimator.MONEY),
  MONEY_ARRAY(791, true, Money[].class, null, DataTypeEstimator.MONEY),
  BIT(1560, true, Object.class, JDBCType.BIT, DataTypeEstimator.UNSUPPORTED),
  BIT_ARRAY(1561, true, Object[].class, JDBCType.BIT, DataTypeEstimator.UNSUPPORTED),
  VARBIT(1562, true, Object.class, JDBCType.OTHER, DataTypeEstimator.UNSUPPORTED),
  VARBIT_ARRAY(1563, true, Object[].class, JDBCType.BIT, DataTypeEstimator.UNSUPPORTED),
  CHAR(18, true, String.class, JDBCType.BIT, null, Tuple::getString, DataTypeEstimator.CHAR),
  CHAR_ARRAY(1002, true, String[].class, JDBCType.CHAR, null, Tuple::getArrayOfStrings, DataTypeEstimator.CHAR),
  VARCHAR(1043, true, String.class, JDBCType.VARCHAR, null, Tuple::getString, DataTypeEstimator.VARCHAR),
  VARCHAR_ARRAY(1015, true, String[].class, JDBCType.VARCHAR, null, Tuple::getArrayOfStrings, DataTypeEstimator.VARCHAR),
  BPCHAR(1042, true, String.class, JDBCType.VARCHAR, null, Tuple::getString, DataTypeEstimator.BPCHAR),
  BPCHAR_ARRAY(1014, true, String[].class, JDBCType.VARCHAR, null, Tuple::getArrayOfStrings, DataTypeEstimator.BPCHAR),
  TEXT(25, true, String.class, JDBCType.LONGVARCHAR, null, Tuple::getString, DataTypeEstimator.TEXT),
  TEXT_ARRAY(1009, true, String[].class, JDBCType.LONGVARCHAR, null, Tuple::getArrayOfStrings, DataTypeEstimator.TEXT),
  NAME(19, true, String.class, JDBCType.VARCHAR, null, Tuple::getString, DataTypeEstimator.NAME),
  NAME_ARRAY(1003, true, String[].class, JDBCType.VARCHAR, null, Tuple::getArrayOfStrings, DataTypeEstimator.NAME),
  DATE(1082, true, LocalDate.class, JDBCType.DATE, null, Tuple::getLocalDate, DataTypeEstimator.DATE),
  DATE_ARRAY(1182, true, LocalDate[].class, JDBCType.DATE, null, Tuple::getArrayOfLocalDates, DataTypeEstimator.DATE),
  TIME(1083, true, LocalTime.class, JDBCType.TIME, null, Tuple::getLocalTime, DataTypeEstimator.TIME),
  TIME_ARRAY(1183, true, LocalTime[].class, JDBCType.TIME, null, Tuple::getArrayOfLocalTimes, DataTypeEstimator.TIME),
  TIMETZ(1266, true, OffsetTime.class, JDBCType.TIME_WITH_TIMEZONE, null, Tuple::getOffsetTime, DataTypeEstimator.TIMETZ),
  TIMETZ_ARRAY(1270, true, OffsetTime[].class, JDBCType.TIME_WITH_TIMEZONE, null, Tuple::getArrayOfOffsetTimes, DataTypeEstimator.TIMETZ),
  TIMESTAMP(1114, true, LocalDateTime.class, JDBCType.TIMESTAMP, null, Tuple::getLocalDateTime, DataTypeEstimator.TIMESTAMP),
  TIMESTAMP_ARRAY(1115, true, LocalDateTime[].class, JDBCType.TIMESTAMP, null, Tuple::getArrayOfLocalDateTimes, DataTypeEstimator.TIMESTAMP),
  TIMESTAMPTZ(1184, true, OffsetDateTime.class, JDBCType.TIMESTAMP_WITH_TIMEZONE, null, Tuple::getOffsetDateTime, DataTypeEstimator.TIMESTAMPTZ),
  TIMESTAMPTZ_ARRAY(1185, true, OffsetDateTime[].class, JDBCType.TIMESTAMP_WITH_TIMEZONE, null, Tuple::getArrayOfOffsetDateTimes, DataTypeEstimator.TIMESTAMPTZ),
  INTERVAL(1186, true, Interval.class, JDBCType.DATE, DataTypeEstimator.INTERVAL),
  INTERVAL_ARRAY(1187, true, Interval[].class, JDBCType.DATE, DataTypeEstimator.INTERVAL),
  BYTEA(17, true, Buffer.class, JDBCType.BINARY, null, Tuple::getBuffer, DataTypeEstimator.BYTEA),
  BYTEA_ARRAY(1001, true, Buffer[].class, JDBCType.BINARY, null, Tuple::getArrayOfBuffers, DataTypeEstimator.BYTEA),
  MACADDR(829, true, Object.class, JDBCType.OTHER, DataTypeEstimator.UNSUPPORTED),
  INET(869, true, Inet.class, JDBCType.OTHER, DataTypeEstimator.INET),
  INET_ARRAY(1041, true, Inet[].class, JDBCType.OTHER, DataTypeEstimator.INET),
  CIDR(650, true, Cidr.class, JDBCType.OTHER, DataTypeEstimator.CIDR),
  MACADDR8(774, true, Object[].class, JDBCType.OTHER, DataTypeEstimator.UNSUPPORTED),
  UUID(2950, true, UUID.class, JDBCType.OTHER, null, Tuple::getUUID, DataTypeEstimator.UUID),
  UUID_ARRAY(2951, true, UUID[].class, JDBCType.OTHER, null, Tuple::getArrayOfUUIDs, DataTypeEstimator.UUID),
  JSON(114, true, Object.class, JDBCType.OTHER, ParamExtractor::prepareJson, Tuple::getJson, DataTypeEstimator.JSON),
  JSON_ARRAY(199, true, Object[].class, JDBCType.OTHER, ParamExtractor::prepareJson, Tuple::getArrayOfJsons, DataTypeEstimator.JSON),
  JSONB(3802, true, Object.class, JDBCType.OTHER,  ParamExtractor::prepareJson, Tuple::getJson, DataTypeEstimator.JSONB),
  JSONB_ARRAY(3807, true, Object[].class, JDBCType.OTHER,  ParamExtractor::prepareJson, Tuple::getArrayOfJsons, DataTypeEstimator.JSONB),
  XML(142, true, Object.class, JDBCType.OTHER, DataTypeEstimator.UNSUPPORTED),
  XML_ARRAY(143, true, Object[].class, JDBCType.OTHER, DataTypeEstimator.UNSUPPORTED),
  POINT(600, true, Point.class, JDBCType.OTHER, DataTypeEstimator.POINT),
  POINT_ARRAY(1017, true, Point[].class, JDBCType.OTHER, DataTypeEstimator.POINT),
  LINE(628, true, Line.class, JDBCType.OTHER, DataTypeEstimator.LINE),
  LINE_ARRAY(629, true, Line[].class, JDBCType.OTHER, DataTypeEstimator.LINE),
  LSEG(601, true, LineSegment.class, JDBCType.OTHER, DataTypeEstimator.LSEG),
  LSEG_ARRAY(1018, true, LineSegment[].class, JDBCType.OTHER, DataTypeEstimator.LSEG),
  BOX(603, true, Box.class, JDBCType.OTHER, DataTypeEstimator.BOX),
  BOX_ARRAY(1020, true, Box[].class, JDBCType.OTHER, DataTypeEstimator.BOX),
  PATH(602, true, Path.class, JDBCType.OTHER, DataTypeEstimator.PATH),
  PATH_ARRAY(1019, true, Path[].class, JDBCType.OTHER, DataTypeEstimator.PATH),
  POLYGON(604, true, Polygon.class, JDBCType.OTHER, DataTypeEstimator.POLYGON),
  POLYGON_ARRAY(1027, true, Polygon[].class, JDBCType.OTHER, DataTypeEstimator.POLYGON),
  CIRCLE(718, true, Circle.class, JDBCType.OTHER, DataTypeEstimator.CIRCLE),
  CIRCLE_ARRAY(719, true, Circle[].class, JDBCType.OTHER, DataTypeEstimator.CIRCLE),
  HSTORE(33670, true, Object.class, JDBCType.OTHER, DataTypeEstimator.UNSUPPORTED),
  OID(26, true, Object.class, JDBCType.OTHER, DataTypeEstimator.UNSUPPORTED),
  OID_ARRAY(1028, true, Object[].class, JDBCType.OTHER, DataTypeEstimator.UNSUPPORTED),
  VOID(2278, true, Object.class, JDBCType.OTHER, DataTypeEstimator.UNSUPPORTED),
  UNKNOWN(705, false, String.class, JDBCType.OTHER, ParamExtractor::prepareUnknown, ParamExtractor::extractUnknownType, DataTypeEstimator.UNKNOWN),
  TS_VECTOR(3614, false, String.class, JDBCType.OTHER, DataTypeEstimator.UNSUPPORTED),
  TS_VECTOR_ARRAY(3643, false, String[].class, JDBCType.OTHER, DataTypeEstimator.UNSUPPORTED),
  TS_QUERY(3615, false,  String.class, JDBCType.OTHER, DataTypeEstimator.UNSUPPORTED),
  TS_QUERY_ARRAY(3645, false,  String[].class, JDBCType.OTHER, DataTypeEstimator.UNSUPPORTED);

  private static final Logger logger = LoggerFactory.getLogger(DataType.class);
  private static final IntObjectMap<DataType> oidToDataType = new IntObjectHashMap<>();
  private static final Map<Class<?>, DataType> encodingTypeToDataType = new HashMap<>();

  final int id;
  final boolean array;
  final boolean supportsBinary;
  final Class<?> encodingType; // Not really used for now
  final Class<?> decodingType;
  final JDBCType jdbcType;
  final ParamExtractor<?> paramExtractor;
  final Function<Object, Object> preEncoder;

  // > 0 : size
  // < 0 : switch
  final int lengthEstimator; //

  <T> DataType(int id, boolean supportsBinary, Class<T> type, JDBCType jdbcType, Function<Object, Object> preEncoder, ParamExtractor<T> paramExtractor, int lengthEstimator) {
    this(id, supportsBinary, type, type, jdbcType, paramExtractor, preEncoder, lengthEstimator);
  }

  <T> DataType(int id, boolean supportsBinary, Class<T> type, JDBCType jdbcType, int lengthEstimator) {
    this(id, supportsBinary, type, type, jdbcType, null, null, lengthEstimator);
  }

  <T> DataType(int id, boolean supportsBinary, Class<T> encodingType, Class<?> decodingType, JDBCType jdbcType, ParamExtractor<T> paramExtractor, Function<Object, Object> preEncoder, int lengthEstimator) {
    this.id = id;
    this.supportsBinary = supportsBinary;
    this.encodingType = Objects.requireNonNull(encodingType);
    this.decodingType = decodingType;
    this.jdbcType = jdbcType;
    this.array = decodingType.isArray();
    this.paramExtractor = paramExtractor != null ? paramExtractor : new DefaultParamExtractor<>(encodingType);
    this.preEncoder = preEncoder;
    this.lengthEstimator = lengthEstimator;
  }

  static DataType valueOf(int oid) {
    DataType value = oidToDataType.get(oid);
    if (value == null) {
      logger.debug("Postgres type OID=" + oid + " not handled - using unknown type instead");
      return UNKNOWN;
    } else {
      return value;
    }
  }

  static DataType lookup(Class<?> type) {
    DataType dataType = encodingTypeToDataType.get(type);
    if (dataType == null) {
      if (Buffer.class.isAssignableFrom(type)) {
        return BYTEA;
      }
      dataType = DataType.UNKNOWN;
    }
    return dataType;
  }

  static {
    for (DataType dataType : values()) {
      oidToDataType.put(dataType.id, dataType);
    }
    encodingTypeToDataType.put(String.class, VARCHAR);
    encodingTypeToDataType.put(String[].class, VARCHAR_ARRAY);
    encodingTypeToDataType.put(Boolean.class, BOOL);
    encodingTypeToDataType.put(Boolean[].class, BOOL_ARRAY);
    encodingTypeToDataType.put(Short.class, INT2);
    encodingTypeToDataType.put(Short[].class, INT2_ARRAY);
    encodingTypeToDataType.put(Integer.class, INT4);
    encodingTypeToDataType.put(Integer[].class, INT4_ARRAY);
    encodingTypeToDataType.put(Long.class, INT8);
    encodingTypeToDataType.put(Long[].class, INT8_ARRAY);
    encodingTypeToDataType.put(Float.class, FLOAT4);
    encodingTypeToDataType.put(Float[].class, FLOAT4_ARRAY);
    encodingTypeToDataType.put(Double.class, FLOAT8);
    encodingTypeToDataType.put(Double[].class, FLOAT8_ARRAY);
    encodingTypeToDataType.put(LocalDate.class, DATE);
    encodingTypeToDataType.put(LocalDate[].class, DATE_ARRAY);
    encodingTypeToDataType.put(LocalDateTime.class, TIMESTAMP);
    encodingTypeToDataType.put(LocalDateTime[].class, TIMESTAMP_ARRAY);
    encodingTypeToDataType.put(OffsetDateTime.class, TIMESTAMPTZ);
    encodingTypeToDataType.put(OffsetDateTime[].class, TIMESTAMPTZ_ARRAY);
    encodingTypeToDataType.put(Interval.class, INTERVAL);
    encodingTypeToDataType.put(Interval[].class, INTERVAL_ARRAY);
    encodingTypeToDataType.put(Buffer[].class, BYTEA_ARRAY);
    encodingTypeToDataType.put(UUID.class, UUID);
    encodingTypeToDataType.put(UUID[].class, UUID_ARRAY);
    encodingTypeToDataType.put(JsonObject.class, JSON);
    encodingTypeToDataType.put(JsonObject[].class, JSON_ARRAY);
    encodingTypeToDataType.put(JsonArray.class, JSON);
    encodingTypeToDataType.put(JsonArray[].class, JSON_ARRAY);
    encodingTypeToDataType.put(Point.class, POINT);
    encodingTypeToDataType.put(Point[].class, POINT_ARRAY);
    encodingTypeToDataType.put(Line.class, LINE);
    encodingTypeToDataType.put(Line[].class, LINE_ARRAY);
    encodingTypeToDataType.put(LineSegment.class, LSEG);
    encodingTypeToDataType.put(LineSegment[].class, LSEG_ARRAY);
    encodingTypeToDataType.put(Box.class, BOX);
    encodingTypeToDataType.put(Box[].class, BOX_ARRAY);
    encodingTypeToDataType.put(Path.class, PATH);
    encodingTypeToDataType.put(Path[].class, PATH_ARRAY);
    encodingTypeToDataType.put(Polygon.class, POLYGON);
    encodingTypeToDataType.put(Polygon[].class, POLYGON_ARRAY);
    encodingTypeToDataType.put(Circle.class, CIRCLE);
    encodingTypeToDataType.put(Circle[].class, CIRCLE_ARRAY);
  }

  private static class DefaultParamExtractor<T> implements ParamExtractor<T> {
    static final RuntimeException FAILURE = new VertxException("ignored", true);

    final Class<T> encodingType;

    DefaultParamExtractor(Class<T> encodingType) {
      this.encodingType = encodingType;
    }

    @Override
    public T get(TupleInternal tuple, int idx) {
      Object value = tuple.getValue(idx);
      if (value == null) {
        return null;
      }
      if (encodingType.isAssignableFrom(value.getClass())) {
        return encodingType.cast(value);
      }
      throw FAILURE;
    }
  }
}

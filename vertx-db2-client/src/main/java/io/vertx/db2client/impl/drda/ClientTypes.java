/*
 * Copyright (C) 2019,2020 IBM Corporation
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
 */
package io.vertx.db2client.impl.drda;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.RowId;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import io.netty.buffer.ByteBuf;

// This enumeration of types represents the typing scheme used by our jdbc driver.
// Once this is finished, we need to review our switches to make sure they are exhaustive
/**
 * Information about DB2 data types. See: <a href=https://www.ibm.com/support/knowledgecenter/SSEPEK_12.0.0/intro/src/tpc/db2z_datatypes.html>
 * DB2 Data Types</a>
 */
public class ClientTypes {

    public final static int BIT = Types.BIT; // -7

    // final static int TINYINT = Types.TINYINT; // -6;

    public final static int BOOLEAN = Types.BOOLEAN; // 16

    public final static int SMALLINT = Types.SMALLINT; // 5

    public final static int INTEGER = Types.INTEGER; // 4

    public final static int BIGINT = Types.BIGINT; // -5;

    // We type using DOUBLE
    // final static int FLOAT = Types.FLOAT; // 6;

    public final static int REAL = Types.REAL; // 7;

    public final static int DOUBLE = Types.DOUBLE; // 8;

    // We type using DECIMAL
    // final static int NUMERIC = Types.NUMERIC; // 2;

    public final static int DECIMAL = Types.DECIMAL; // 3;

    public final static int CHAR = Types.CHAR; // 1;

    public final static int VARCHAR = Types.VARCHAR; // 12;

    public final static int LONGVARCHAR = Types.LONGVARCHAR; // -1;

    public final static int DATE = Types.DATE; // 91;

    public final static int TIME = Types.TIME; // 92;

    public final static int TIMESTAMP = Types.TIMESTAMP; // 93;

    public final static int BINARY = Types.BINARY; // -2;

    public final static int VARBINARY = Types.VARBINARY; // -3;

    public final static int LONGVARBINARY = Types.LONGVARBINARY; // -4;

    public final static int BLOB = Types.BLOB; // 2004;

    public final static int CLOB = Types.CLOB; // 2005;

    public final static int JAVA_OBJECT = Types.JAVA_OBJECT; // 2000;

    public final static int ROWID = Types.ROWID; // -8

    private ClientTypes() {
    }

    public static String getTypeString(int type) {
        switch (type) {
        case BIGINT:
            return "BIGINT";
        case BINARY:
            return "BINARY";
        case BLOB:
            return "BLOB";
        case BIT:
        case BOOLEAN:
            return "BOOLEAN";
        case CHAR:
            return "CHAR";
        case CLOB:
            return "CLOB";
        case DATE:
            return "DATE";
        case DECIMAL:
            return "DECIMAL";
        case DOUBLE:
            return "DOUBLE";
        case INTEGER:
            return "INTEGER";
        case LONGVARBINARY:
            return "LONGVARBINARY";
        case LONGVARCHAR:
            return "LONGVARCHAR";
        case REAL:
            return "REAL";
        case SMALLINT:
            return "SMALLINT";
        case TIME:
            return "TIME";
        case TIMESTAMP:
            return "TIMESTAMP";
        case VARBINARY:
            return "VARBINARY";
        case VARCHAR:
            return "VARCHAR";
        case ROWID:
            return "ROWID";
        // Types we don't support:
        case Types.ARRAY:
            return "ARRAY";
        case Types.DATALINK:
            return "DATALINK";
        case Types.REF:
            return "REF";
        case Types.SQLXML:
            return "SQLXML";
        case Types.STRUCT:
            return "STRUCT";
        // Unknown type:
        default:
            return "UNKNOWN(" + type + ")";
        }
    }

    public static Class<?> preferredJavaType(int clientType) {
      switch (clientType) {
      case BIGINT:
        return BigInteger.class;
      case BINARY:
      case LONGVARBINARY:
      case VARBINARY:
        return ByteBuf.class;
      case BIT:
        return Boolean.class;
      case BLOB:
        return ByteBuf.class;
      case BOOLEAN:
        return Boolean.class;
      case CHAR:
      case LONGVARCHAR:
      case VARCHAR:
        return String.class;
      case CLOB:
        return String.class;
      case DATE:
        return LocalDate.class;
      case DECIMAL:
        return BigDecimal.class;
      case DOUBLE:
        return Double.class;
      case INTEGER:
        return Integer.class;
      case REAL:
        return Float.class;
      case ROWID:
        return DB2RowId.class;
      case SMALLINT:
        return Short.class;
      case TIME:
        return LocalTime.class;
      case TIMESTAMP:
        return LocalDateTime.class;
      default:
        throw new IllegalArgumentException("Unknown client type: " + clientType);
      }
    }

    static public int mapDB2TypeToDriverType(boolean isDescribed, int sqlType, long length, int ccsid) {
        switch (sqlType & ~1) { // Utils.getNonNullableSqlType(sqlType)) { // mask the isNullable bit
        case DRDAConstants.DB2_SQLTYPE_BOOLEAN:
            return BOOLEAN;
        case DRDAConstants.DB2_SQLTYPE_SMALL:
            return SMALLINT;
        case DRDAConstants.DB2_SQLTYPE_INTEGER:
            return INTEGER;
        case DRDAConstants.DB2_SQLTYPE_BIGINT:
            return BIGINT;
        case DRDAConstants.DB2_SQLTYPE_FLOAT:
            if (length == 16) // can map to either NUMERIC or DECIMAL
            {
                return DECIMAL;
            } else if (length == 8) // can map to either DOUBLE or FLOAT
            {
                return DOUBLE;
            } else if (length == 4) {
                return REAL;
            } else {
                return 0;
            }
        case DRDAConstants.DB2_SQLTYPE_DECIMAL: // can map to either NUMERIC or DECIMAL
        case DRDAConstants.DB2_SQLTYPE_NUMERIC: // can map to either NUMERIC or DECIMAL
            return DECIMAL;
        case DRDAConstants.DB2_SQLTYPE_CHAR: // mixed and single byte
            if (isDescribed && (ccsid == 0xffff || ccsid == 0)) {
                return BINARY;
            } else {
                return CHAR;
            }
        case DRDAConstants.DB2_SQLTYPE_CSTR: // null terminated SBCS/Mixed
            return CHAR;
        // use ccsid to distinguish between BINARY and CHAR, VARBINARY and VARCHAR,
        // LONG...
        case DRDAConstants.DB2_SQLTYPE_VARCHAR: // variable character SBCS/Mixed
            if (isDescribed && (ccsid == 0xffff || ccsid == 0)) {
                return VARBINARY;
            } else {
                return VARCHAR;
            }
        case DRDAConstants.DB2_SQLTYPE_LONG: // long varchar SBCS/Mixed
            if (isDescribed && (ccsid == 0xffff || ccsid == 0)) {
                return LONGVARBINARY;
            } else {
                return LONGVARCHAR;
            }
        case DRDAConstants.DB2_SQLTYPE_DATE:
            return DATE;
        case DRDAConstants.DB2_SQLTYPE_TIME:
            return TIME;
        case DRDAConstants.DB2_SQLTYPE_TIMESTAMP:
            return TIMESTAMP;
        case DRDAConstants.DB2_SQLTYPE_CLOB: // large object character SBCS/Mixed
            return ClientTypes.CLOB;
        case DRDAConstants.DB2_SQLTYPE_BLOB: // large object bytes
            return Types.BLOB;
        case DRDAConstants.DB2_SQLTYPE_FAKE_UDT: // user defined types
            return Types.JAVA_OBJECT;
        case DRDAConstants.DB2_SQLTYPE_ROWID:
            return ROWID;
        default:
            // TODO: log a warning here
            // System.out.println("WARN: Unknown DB2 type encountered: " + sqlType);
            return 0;
        }
    }

    public static boolean canConvert(Object value, int toType) {
      if (value == null)
        return true;

      Class<?> clazz = value.getClass();
      // Everything can convert to String
      switch (toType) {
        case ClientTypes.INTEGER:
        case ClientTypes.BIGINT:
          return clazz == boolean.class ||
               clazz == Boolean.class ||
               clazz == double.class ||
               clazz == Double.class ||
               clazz == float.class ||
               clazz == Float.class ||
               clazz == int.class ||
               clazz == Integer.class ||
               clazz == long.class ||
               clazz == Long.class ||
               clazz == short.class ||
               clazz == Short.class ||
               clazz == BigDecimal.class ||
               clazz == BigInteger.class;
        case ClientTypes.DOUBLE:
        case ClientTypes.REAL:
        case ClientTypes.DECIMAL:
       return clazz == double.class ||
            clazz == Double.class ||
            clazz == float.class ||
            clazz == Float.class ||
            clazz == int.class ||
            clazz == Integer.class ||
            clazz == long.class ||
            clazz == Long.class ||
            clazz == short.class ||
            clazz == Short.class ||
            clazz == BigDecimal.class ||
            clazz == BigInteger.class;
        case ClientTypes.BIT:
        case ClientTypes.BOOLEAN:
        case ClientTypes.SMALLINT:
          return clazz == boolean.class ||
            clazz == Boolean.class ||
            clazz == char.class ||
            clazz == Character.class ||
            clazz == int.class ||
            clazz == Integer.class ||
            clazz == long.class ||
            clazz == Long.class ||
            clazz == short.class ||
            clazz == Short.class ||
            clazz == byte.class ||
            clazz == Byte.class ||
            clazz == BigDecimal.class;
        case ClientTypes.BINARY:
          return clazz == boolean.class ||
               clazz == Boolean.class ||
                 clazz == byte.class ||
                 clazz == Byte.class ||
                 clazz == byte[].class;
        case ClientTypes.DATE:
          return clazz == java.time.LocalDate.class ||
               clazz == java.sql.Date.class ||
               clazz == String.class;
        case ClientTypes.TIME:
          return clazz == java.time.LocalTime.class ||
                 clazz == java.sql.Time.class ||
                 clazz == String.class;
        case ClientTypes.TIMESTAMP:
          return clazz == java.time.LocalDateTime.class ||
                 clazz == java.sql.Timestamp.class ||
                 clazz == String.class;
        case ClientTypes.CHAR:
            return clazz == char.class ||
                   clazz == Character.class ||
                   clazz == String.class;
        case ClientTypes.VARCHAR:
        case ClientTypes.LONGVARCHAR:
        case ClientTypes.CLOB:
          return clazz == String.class ||
                 clazz == char[].class ||
                 clazz == UUID.class;
        case ClientTypes.VARBINARY:
        case ClientTypes.LONGVARBINARY:
        case ClientTypes.BLOB:
            return clazz == byte[].class ||
                   ByteBuf.class.isAssignableFrom(clazz);
        case ClientTypes.ROWID:
            return clazz == RowId.class ||
                   clazz == DB2RowId.class;
      }
      return false;
    }
}

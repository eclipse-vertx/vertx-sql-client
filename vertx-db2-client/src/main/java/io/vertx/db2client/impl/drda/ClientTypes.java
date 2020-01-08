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

import java.sql.Types;

// This enumeration of types represents the typing scheme used by our jdbc driver.
// Once this is finished, we need to review our switches to make sure they are exhaustive

public class ClientTypes {
    // -------------------------------- Driver types
    // -------------------------------------------------

    final static int BIT = Types.BIT; // -7;

    // Not currently supported as a DERBY column type. Mapped to SMALLINT.
    // final static int TINYINT = Types.TINYINT; // -6;

    final static int BOOLEAN = Types.BOOLEAN; // 16;

    final static int SMALLINT = Types.SMALLINT; // 5;

    final static int INTEGER = Types.INTEGER; // 4;

    final static int BIGINT = Types.BIGINT; // -5;

    // We type using DOUBLE
    // final static int FLOAT = Types.FLOAT; // 6;

    final static int REAL = Types.REAL; // 7;

    final static int DOUBLE = Types.DOUBLE; // 8;

    // We type using DECIMAL
    // final static int NUMERIC = Types.NUMERIC; // 2;

    final static int DECIMAL = Types.DECIMAL; // 3;

    public final static int CHAR = Types.CHAR; // 1;

    public final static int VARCHAR = Types.VARCHAR; // 12;

    public final static int LONGVARCHAR = Types.LONGVARCHAR; // -1;

    final static int DATE = Types.DATE; // 91;

    final static int TIME = Types.TIME; // 92;

    final static int TIMESTAMP = Types.TIMESTAMP; // 93;

    public final static int BINARY = Types.BINARY; // -2;

    public final static int VARBINARY = Types.VARBINARY; // -3;

    public final static int LONGVARBINARY = Types.LONGVARBINARY; // -4;

    public final static int BLOB = Types.BLOB; // 2004;

    public final static int CLOB = Types.CLOB; // 2005;

    public final static int JAVA_OBJECT = Types.JAVA_OBJECT; // 2000;

    // hide the default constructor
    private ClientTypes() {
    }

    static String getTypeString(int type) {
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
        // Types we don't support:
        case Types.ARRAY:
            return "ARRAY";
        case Types.DATALINK:
            return "DATALINK";
        case Types.REF:
            return "REF";
        case Types.ROWID:
            return "ROWID";
        case Types.SQLXML:
            return "SQLXML";
        case Types.STRUCT:
            return "STRUCT";
        // Unknown type:
        default:
            return "<UNKNOWN>";
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
        default:
            return 0;
        }
    }
}

/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.pgclient;

/**
 * SQLSTATE codes used by PostgreSQL
 * (<a href="https://www.postgresql.org/docs/current/errcodes-appendix.html">PostgreSQL Error Codes</a>,
 * <a href="https://git.postgresql.org/gitweb/?p=postgresql.git;a=blob;f=src/backend/utils/errcodes.txt;hb=HEAD">
 * errcodes.txt</a>)
 */
public class PgErrorCodes {

  public static final String ERRCODE_SUCCESSFUL_COMPLETION = "00000";
  public static final String ERRCODE_WARNING = "01000";
  public static final String ERRCODE_WARNING_NULL_VALUE_ELIMINATED_IN_SET_FUNCTION = "01003";
  public static final String ERRCODE_WARNING_STRING_DATA_RIGHT_TRUNCATION = "01004";
  public static final String ERRCODE_WARNING_PRIVILEGE_NOT_REVOKED = "01006";
  public static final String ERRCODE_WARNING_PRIVILEGE_NOT_GRANTED = "01007";
  public static final String ERRCODE_WARNING_IMPLICIT_ZERO_BIT_PADDING = "01008";
  public static final String ERRCODE_WARNING_DYNAMIC_RESULT_SETS_RETURNED = "0100C";
  public static final String ERRCODE_WARNING_DEPRECATED_FEATURE = "01P01";
  public static final String ERRCODE_NO_DATA = "02000";
  public static final String ERRCODE_NO_ADDITIONAL_DYNAMIC_RESULT_SETS_RETURNED = "02001";
  public static final String ERRCODE_SQL_STATEMENT_NOT_YET_COMPLETE = "03000";
  public static final String ERRCODE_CONNECTION_EXCEPTION = "08000";
  public static final String ERRCODE_SQLCLIENT_UNABLE_TO_ESTABLISH_SQLCONNECTION = "08001";
  public static final String ERRCODE_CONNECTION_DOES_NOT_EXIST = "08003";
  public static final String ERRCODE_SQLSERVER_REJECTED_ESTABLISHMENT_OF_SQLCONNECTION = "08004";
  public static final String ERRCODE_CONNECTION_FAILURE = "08006";
  public static final String ERRCODE_TRANSACTION_RESOLUTION_UNKNOWN = "08007";
  public static final String ERRCODE_PROTOCOL_VIOLATION = "08P01";
  public static final String ERRCODE_TRIGGERED_ACTION_EXCEPTION = "09000";
  public static final String ERRCODE_FEATURE_NOT_SUPPORTED = "0A000";
  public static final String ERRCODE_INVALID_TRANSACTION_INITIATION = "0B000";
  public static final String ERRCODE_LOCATOR_EXCEPTION = "0F000";
  public static final String ERRCODE_L_E_INVALID_SPECIFICATION = "0F001";
  public static final String ERRCODE_INVALID_GRANTOR = "0L000";
  public static final String ERRCODE_INVALID_GRANT_OPERATION = "0LP01";
  public static final String ERRCODE_INVALID_ROLE_SPECIFICATION = "0P000";
  public static final String ERRCODE_DIAGNOSTICS_EXCEPTION = "0Z000";
  public static final String ERRCODE_STACKED_DIAGNOSTICS_ACCESSED_WITHOUT_ACTIVE_HANDLER = "0Z002";
  public static final String ERRCODE_CASE_NOT_FOUND = "20000";
  public static final String ERRCODE_CARDINALITY_VIOLATION = "21000";
  public static final String ERRCODE_DATA_EXCEPTION = "22000";
  public static final String ERRCODE_STRING_DATA_RIGHT_TRUNCATION = "22001";
  public static final String ERRCODE_NULL_VALUE_NO_INDICATOR_PARAMETER = "22002";
  public static final String ERRCODE_NUMERIC_VALUE_OUT_OF_RANGE = "22003";
  public static final String ERRCODE_NULL_VALUE_NOT_ALLOWED = "22004";
  public static final String ERRCODE_ERROR_IN_ASSIGNMENT = "22005";
  public static final String ERRCODE_INVALID_DATETIME_FORMAT = "22007";
  public static final String ERRCODE_DATETIME_FIELD_OVERFLOW = "22008";
  public static final String ERRCODE_DATETIME_VALUE_OUT_OF_RANGE = "22008";
  public static final String ERRCODE_INVALID_TIME_ZONE_DISPLACEMENT_VALUE = "22009";
  public static final String ERRCODE_ESCAPE_CHARACTER_CONFLICT = "2200B";
  public static final String ERRCODE_INVALID_USE_OF_ESCAPE_CHARACTER = "2200C";
  public static final String ERRCODE_INVALID_ESCAPE_OCTET = "2200D";
  public static final String ERRCODE_ZERO_LENGTH_CHARACTER_STRING = "2200F";
  public static final String ERRCODE_MOST_SPECIFIC_TYPE_MISMATCH = "2200G";
  public static final String ERRCODE_SEQUENCE_GENERATOR_LIMIT_EXCEEDED = "2200H";
  public static final String ERRCODE_NOT_AN_XML_DOCUMENT = "2200L";
  public static final String ERRCODE_INVALID_XML_DOCUMENT = "2200M";
  public static final String ERRCODE_INVALID_XML_CONTENT = "2200N";
  public static final String ERRCODE_INVALID_XML_COMMENT = "2200S";
  public static final String ERRCODE_INVALID_XML_PROCESSING_INSTRUCTION = "2200T";
  public static final String ERRCODE_INVALID_INDICATOR_PARAMETER_VALUE = "22010";
  public static final String ERRCODE_SUBSTRING_ERROR = "22011";
  public static final String ERRCODE_DIVISION_BY_ZERO = "22012";
  public static final String ERRCODE_INVALID_PRECEDING_OR_FOLLOWING_SIZE = "22013";
  public static final String ERRCODE_INVALID_ARGUMENT_FOR_NTILE = "22014";
  public static final String ERRCODE_INTERVAL_FIELD_OVERFLOW = "22015";
  public static final String ERRCODE_INVALID_ARGUMENT_FOR_NTH_VALUE = "22016";
  public static final String ERRCODE_INVALID_CHARACTER_VALUE_FOR_CAST = "22018";
  public static final String ERRCODE_INVALID_ESCAPE_CHARACTER = "22019";
  public static final String ERRCODE_INVALID_REGULAR_EXPRESSION = "2201B";
  public static final String ERRCODE_INVALID_ARGUMENT_FOR_LOG = "2201E";
  public static final String ERRCODE_INVALID_ARGUMENT_FOR_POWER_FUNCTION = "2201F";
  public static final String ERRCODE_INVALID_ARGUMENT_FOR_WIDTH_BUCKET_FUNCTION = "2201G";
  public static final String ERRCODE_INVALID_ROW_COUNT_IN_LIMIT_CLAUSE = "2201W";
  public static final String ERRCODE_INVALID_ROW_COUNT_IN_RESULT_OFFSET_CLAUSE = "2201X";
  public static final String ERRCODE_CHARACTER_NOT_IN_REPERTOIRE = "22021";
  public static final String ERRCODE_INDICATOR_OVERFLOW = "22022";
  public static final String ERRCODE_INVALID_PARAMETER_VALUE = "22023";
  public static final String ERRCODE_UNTERMINATED_C_STRING = "22024";
  public static final String ERRCODE_INVALID_ESCAPE_SEQUENCE = "22025";
  public static final String ERRCODE_STRING_DATA_LENGTH_MISMATCH = "22026";
  public static final String ERRCODE_TRIM_ERROR = "22027";
  public static final String ERRCODE_ARRAY_ELEMENT_ERROR = "2202E";
  public static final String ERRCODE_ARRAY_SUBSCRIPT_ERROR = "2202E";
  public static final String ERRCODE_INVALID_TABLESAMPLE_REPEAT = "2202G";
  public static final String ERRCODE_INVALID_TABLESAMPLE_ARGUMENT = "2202H";
  public static final String ERRCODE_DUPLICATE_JSON_OBJECT_KEY_VALUE = "22030";
  public static final String ERRCODE_INVALID_ARGUMENT_FOR_SQL_JSON_DATETIME_FUNCTION = "22031";
  public static final String ERRCODE_INVALID_JSON_TEXT = "22032";
  public static final String ERRCODE_INVALID_SQL_JSON_SUBSCRIPT = "22033";
  public static final String ERRCODE_MORE_THAN_ONE_SQL_JSON_ITEM = "22034";
  public static final String ERRCODE_NO_SQL_JSON_ITEM = "22035";
  public static final String ERRCODE_NON_NUMERIC_SQL_JSON_ITEM = "22036";
  public static final String ERRCODE_NON_UNIQUE_KEYS_IN_A_JSON_OBJECT = "22037";
  public static final String ERRCODE_SINGLETON_SQL_JSON_ITEM_REQUIRED = "22038";
  public static final String ERRCODE_SQL_JSON_ARRAY_NOT_FOUND = "22039";
  public static final String ERRCODE_SQL_JSON_MEMBER_NOT_FOUND = "2203A";
  public static final String ERRCODE_SQL_JSON_NUMBER_NOT_FOUND = "2203B";
  public static final String ERRCODE_SQL_JSON_OBJECT_NOT_FOUND = "2203C";
  public static final String ERRCODE_TOO_MANY_JSON_ARRAY_ELEMENTS = "2203D";
  public static final String ERRCODE_TOO_MANY_JSON_OBJECT_MEMBERS = "2203E";
  public static final String ERRCODE_SQL_JSON_SCALAR_REQUIRED = "2203F";
  public static final String ERRCODE_FLOATING_POINT_EXCEPTION = "22P01";
  public static final String ERRCODE_INVALID_TEXT_REPRESENTATION = "22P02";
  public static final String ERRCODE_INVALID_BINARY_REPRESENTATION = "22P03";
  public static final String ERRCODE_BAD_COPY_FILE_FORMAT = "22P04";
  public static final String ERRCODE_UNTRANSLATABLE_CHARACTER = "22P05";
  public static final String ERRCODE_NONSTANDARD_USE_OF_ESCAPE_CHARACTER = "22P06";
  public static final String ERRCODE_INTEGRITY_CONSTRAINT_VIOLATION = "23000";
  public static final String ERRCODE_RESTRICT_VIOLATION = "23001";
  public static final String ERRCODE_NOT_NULL_VIOLATION = "23502";
  public static final String ERRCODE_FOREIGN_KEY_VIOLATION = "23503";
  public static final String ERRCODE_UNIQUE_VIOLATION = "23505";
  public static final String ERRCODE_CHECK_VIOLATION = "23514";
  public static final String ERRCODE_EXCLUSION_VIOLATION = "23P01";
  public static final String ERRCODE_INVALID_CURSOR_STATE = "24000";
  public static final String ERRCODE_INVALID_TRANSACTION_STATE = "25000";
  public static final String ERRCODE_ACTIVE_SQL_TRANSACTION = "25001";
  public static final String ERRCODE_BRANCH_TRANSACTION_ALREADY_ACTIVE = "25002";
  public static final String ERRCODE_INAPPROPRIATE_ACCESS_MODE_FOR_BRANCH_TRANSACTION = "25003";
  public static final String ERRCODE_INAPPROPRIATE_ISOLATION_LEVEL_FOR_BRANCH_TRANSACTION = "25004";
  public static final String ERRCODE_NO_ACTIVE_SQL_TRANSACTION_FOR_BRANCH_TRANSACTION = "25005";
  public static final String ERRCODE_READ_ONLY_SQL_TRANSACTION = "25006";
  public static final String ERRCODE_SCHEMA_AND_DATA_STATEMENT_MIXING_NOT_SUPPORTED = "25007";
  public static final String ERRCODE_HELD_CURSOR_REQUIRES_SAME_ISOLATION_LEVEL = "25008";
  public static final String ERRCODE_NO_ACTIVE_SQL_TRANSACTION = "25P01";
  public static final String ERRCODE_IN_FAILED_SQL_TRANSACTION = "25P02";
  public static final String ERRCODE_IDLE_IN_TRANSACTION_SESSION_TIMEOUT = "25P03";
  public static final String ERRCODE_INVALID_SQL_STATEMENT_NAME = "26000";
  public static final String ERRCODE_UNDEFINED_PSTATEMENT = "26000";
  public static final String ERRCODE_TRIGGERED_DATA_CHANGE_VIOLATION = "27000";
  public static final String ERRCODE_INVALID_AUTHORIZATION_SPECIFICATION = "28000";
  public static final String ERRCODE_INVALID_PASSWORD = "28P01";
  public static final String ERRCODE_DEPENDENT_PRIVILEGE_DESCRIPTORS_STILL_EXIST = "2B000";
  public static final String ERRCODE_DEPENDENT_OBJECTS_STILL_EXIST = "2BP01";
  public static final String ERRCODE_INVALID_TRANSACTION_TERMINATION = "2D000";
  public static final String ERRCODE_SQL_ROUTINE_EXCEPTION = "2F000";
  public static final String ERRCODE_S_R_E_MODIFYING_SQL_DATA_NOT_PERMITTED = "2F002";
  public static final String ERRCODE_S_R_E_PROHIBITED_SQL_STATEMENT_ATTEMPTED = "2F003";
  public static final String ERRCODE_S_R_E_READING_SQL_DATA_NOT_PERMITTED = "2F004";
  public static final String ERRCODE_S_R_E_FUNCTION_EXECUTED_NO_RETURN_STATEMENT = "2F005";
  public static final String ERRCODE_INVALID_CURSOR_NAME = "34000";
  public static final String ERRCODE_UNDEFINED_CURSOR = "34000";
  public static final String ERRCODE_EXTERNAL_ROUTINE_EXCEPTION = "38000";
  public static final String ERRCODE_E_R_E_CONTAINING_SQL_NOT_PERMITTED = "38001";
  public static final String ERRCODE_E_R_E_MODIFYING_SQL_DATA_NOT_PERMITTED = "38002";
  public static final String ERRCODE_E_R_E_PROHIBITED_SQL_STATEMENT_ATTEMPTED = "38003";
  public static final String ERRCODE_E_R_E_READING_SQL_DATA_NOT_PERMITTED = "38004";
  public static final String ERRCODE_EXTERNAL_ROUTINE_INVOCATION_EXCEPTION = "39000";
  public static final String ERRCODE_E_R_I_E_INVALID_SQLSTATE_RETURNED = "39001";
  public static final String ERRCODE_E_R_I_E_NULL_VALUE_NOT_ALLOWED = "39004";
  public static final String ERRCODE_E_R_I_E_TRIGGER_PROTOCOL_VIOLATED = "39P01";
  public static final String ERRCODE_E_R_I_E_SRF_PROTOCOL_VIOLATED = "39P02";
  public static final String ERRCODE_E_R_I_E_EVENT_TRIGGER_PROTOCOL_VIOLATED = "39P03";
  public static final String ERRCODE_SAVEPOINT_EXCEPTION = "3B000";
  public static final String ERRCODE_S_E_INVALID_SPECIFICATION = "3B001";
  public static final String ERRCODE_INVALID_CATALOG_NAME = "3D000";
  public static final String ERRCODE_UNDEFINED_DATABASE = "3D000";
  public static final String ERRCODE_INVALID_SCHEMA_NAME = "3F000";
  public static final String ERRCODE_UNDEFINED_SCHEMA = "3F000";
  public static final String ERRCODE_TRANSACTION_ROLLBACK = "40000";
  public static final String ERRCODE_T_R_SERIALIZATION_FAILURE = "40001";
  public static final String ERRCODE_T_R_INTEGRITY_CONSTRAINT_VIOLATION = "40002";
  public static final String ERRCODE_T_R_STATEMENT_COMPLETION_UNKNOWN = "40003";
  public static final String ERRCODE_T_R_DEADLOCK_DETECTED = "40P01";
  public static final String ERRCODE_SYNTAX_ERROR_OR_ACCESS_RULE_VIOLATION = "42000";
  public static final String ERRCODE_INSUFFICIENT_PRIVILEGE = "42501";
  public static final String ERRCODE_SYNTAX_ERROR = "42601";
  public static final String ERRCODE_INVALID_NAME = "42602";
  public static final String ERRCODE_INVALID_COLUMN_DEFINITION = "42611";
  public static final String ERRCODE_NAME_TOO_LONG = "42622";
  public static final String ERRCODE_DUPLICATE_COLUMN = "42701";
  public static final String ERRCODE_AMBIGUOUS_COLUMN = "42702";
  public static final String ERRCODE_UNDEFINED_COLUMN = "42703";
  public static final String ERRCODE_UNDEFINED_OBJECT = "42704";
  public static final String ERRCODE_DUPLICATE_OBJECT = "42710";
  public static final String ERRCODE_DUPLICATE_ALIAS = "42712";
  public static final String ERRCODE_DUPLICATE_FUNCTION = "42723";
  public static final String ERRCODE_AMBIGUOUS_FUNCTION = "42725";
  public static final String ERRCODE_GROUPING_ERROR = "42803";
  public static final String ERRCODE_DATATYPE_MISMATCH = "42804";
  public static final String ERRCODE_WRONG_OBJECT_TYPE = "42809";
  public static final String ERRCODE_INVALID_FOREIGN_KEY = "42830";
  public static final String ERRCODE_CANNOT_COERCE = "42846";
  public static final String ERRCODE_UNDEFINED_FUNCTION = "42883";
  public static final String ERRCODE_GENERATED_ALWAYS = "428C9";
  public static final String ERRCODE_RESERVED_NAME = "42939";
  public static final String ERRCODE_UNDEFINED_TABLE = "42P01";
  public static final String ERRCODE_UNDEFINED_PARAMETER = "42P02";
  public static final String ERRCODE_DUPLICATE_CURSOR = "42P03";
  public static final String ERRCODE_DUPLICATE_DATABASE = "42P04";
  public static final String ERRCODE_DUPLICATE_PSTATEMENT = "42P05";
  public static final String ERRCODE_DUPLICATE_SCHEMA = "42P06";
  public static final String ERRCODE_DUPLICATE_TABLE = "42P07";
  public static final String ERRCODE_AMBIGUOUS_PARAMETER = "42P08";
  public static final String ERRCODE_AMBIGUOUS_ALIAS = "42P09";
  public static final String ERRCODE_INVALID_COLUMN_REFERENCE = "42P10";
  public static final String ERRCODE_INVALID_CURSOR_DEFINITION = "42P11";
  public static final String ERRCODE_INVALID_DATABASE_DEFINITION = "42P12";
  public static final String ERRCODE_INVALID_FUNCTION_DEFINITION = "42P13";
  public static final String ERRCODE_INVALID_PSTATEMENT_DEFINITION = "42P14";
  public static final String ERRCODE_INVALID_SCHEMA_DEFINITION = "42P15";
  public static final String ERRCODE_INVALID_TABLE_DEFINITION = "42P16";
  public static final String ERRCODE_INVALID_OBJECT_DEFINITION = "42P17";
  public static final String ERRCODE_INDETERMINATE_DATATYPE = "42P18";
  public static final String ERRCODE_INVALID_RECURSION = "42P19";
  public static final String ERRCODE_WINDOWING_ERROR = "42P20";
  public static final String ERRCODE_COLLATION_MISMATCH = "42P21";
  public static final String ERRCODE_INDETERMINATE_COLLATION = "42P22";
  public static final String ERRCODE_WITH_CHECK_OPTION_VIOLATION = "44000";
  public static final String ERRCODE_INSUFFICIENT_RESOURCES = "53000";
  public static final String ERRCODE_DISK_FULL = "53100";
  public static final String ERRCODE_OUT_OF_MEMORY = "53200";
  public static final String ERRCODE_TOO_MANY_CONNECTIONS = "53300";
  public static final String ERRCODE_CONFIGURATION_LIMIT_EXCEEDED = "53400";
  public static final String ERRCODE_PROGRAM_LIMIT_EXCEEDED = "54000";
  public static final String ERRCODE_STATEMENT_TOO_COMPLEX = "54001";
  public static final String ERRCODE_TOO_MANY_COLUMNS = "54011";
  public static final String ERRCODE_TOO_MANY_ARGUMENTS = "54023";
  public static final String ERRCODE_OBJECT_NOT_IN_PREREQUISITE_STATE = "55000";
  public static final String ERRCODE_OBJECT_IN_USE = "55006";
  public static final String ERRCODE_CANT_CHANGE_RUNTIME_PARAM = "55P02";
  public static final String ERRCODE_LOCK_NOT_AVAILABLE = "55P03";
  public static final String ERRCODE_UNSAFE_NEW_ENUM_VALUE_USAGE = "55P04";
  public static final String ERRCODE_OPERATOR_INTERVENTION = "57000";
  public static final String ERRCODE_QUERY_CANCELED = "57014";
  public static final String ERRCODE_ADMIN_SHUTDOWN = "57P01";
  public static final String ERRCODE_CRASH_SHUTDOWN = "57P02";
  public static final String ERRCODE_CANNOT_CONNECT_NOW = "57P03";
  public static final String ERRCODE_DATABASE_DROPPED = "57P04";
  public static final String ERRCODE_SYSTEM_ERROR = "58000";
  public static final String ERRCODE_IO_ERROR = "58030";
  public static final String ERRCODE_UNDEFINED_FILE = "58P01";
  public static final String ERRCODE_DUPLICATE_FILE = "58P02";
  public static final String ERRCODE_SNAPSHOT_TOO_OLD = "72000";
  public static final String ERRCODE_CONFIG_FILE_ERROR = "F0000";
  public static final String ERRCODE_LOCK_FILE_EXISTS = "F0001";
  public static final String ERRCODE_FDW_ERROR = "HV000";
  public static final String ERRCODE_FDW_OUT_OF_MEMORY = "HV001";
  public static final String ERRCODE_FDW_DYNAMIC_PARAMETER_VALUE_NEEDED = "HV002";
  public static final String ERRCODE_FDW_INVALID_DATA_TYPE = "HV004";
  public static final String ERRCODE_FDW_COLUMN_NAME_NOT_FOUND = "HV005";
  public static final String ERRCODE_FDW_INVALID_DATA_TYPE_DESCRIPTORS = "HV006";
  public static final String ERRCODE_FDW_INVALID_COLUMN_NAME = "HV007";
  public static final String ERRCODE_FDW_INVALID_COLUMN_NUMBER = "HV008";
  public static final String ERRCODE_FDW_INVALID_USE_OF_NULL_POINTER = "HV009";
  public static final String ERRCODE_FDW_INVALID_STRING_FORMAT = "HV00A";
  public static final String ERRCODE_FDW_INVALID_HANDLE = "HV00B";
  public static final String ERRCODE_FDW_INVALID_OPTION_INDEX = "HV00C";
  public static final String ERRCODE_FDW_INVALID_OPTION_NAME = "HV00D";
  public static final String ERRCODE_FDW_OPTION_NAME_NOT_FOUND = "HV00J";
  public static final String ERRCODE_FDW_REPLY_HANDLE = "HV00K";
  public static final String ERRCODE_FDW_UNABLE_TO_CREATE_EXECUTION = "HV00L";
  public static final String ERRCODE_FDW_UNABLE_TO_CREATE_REPLY = "HV00M";
  public static final String ERRCODE_FDW_UNABLE_TO_ESTABLISH_CONNECTION = "HV00N";
  public static final String ERRCODE_FDW_NO_SCHEMAS = "HV00P";
  public static final String ERRCODE_FDW_SCHEMA_NOT_FOUND = "HV00Q";
  public static final String ERRCODE_FDW_TABLE_NOT_FOUND = "HV00R";
  public static final String ERRCODE_FDW_FUNCTION_SEQUENCE_ERROR = "HV010";
  public static final String ERRCODE_FDW_TOO_MANY_HANDLES = "HV014";
  public static final String ERRCODE_FDW_INCONSISTENT_DESCRIPTOR_INFORMATION = "HV021";
  public static final String ERRCODE_FDW_INVALID_ATTRIBUTE_VALUE = "HV024";
  public static final String ERRCODE_FDW_INVALID_STRING_LENGTH_OR_BUFFER_LENGTH = "HV090";
  public static final String ERRCODE_FDW_INVALID_DESCRIPTOR_FIELD_IDENTIFIER = "HV091";
  public static final String ERRCODE_PLPGSQL_ERROR = "P0000";
  public static final String ERRCODE_RAISE_EXCEPTION = "P0001";
  public static final String ERRCODE_NO_DATA_FOUND = "P0002";
  public static final String ERRCODE_TOO_MANY_ROWS = "P0003";
  public static final String ERRCODE_ASSERT_FAILURE = "P0004";
  public static final String ERRCODE_INTERNAL_ERROR = "XX000";
  public static final String ERRCODE_DATA_CORRUPTED = "XX001";
  public static final String ERRCODE_INDEX_CORRUPTED = "XX002";

  private static String code(Throwable throwable) {
    if (throwable == null) {
      return null;
    }
    if (throwable instanceof PgException) {
      return ((PgException) throwable).getCode();
    }
    if (throwable.getCause() instanceof PgException) {
      return ((PgException) throwable.getCause()).getCode();
    }
    return null;
  }

  /**
   * @return throwable or its cause is a PgException with the SQLSTATE code
   */
  public static boolean hasCode(Throwable throwable, String code) {
    return code.equals(code(throwable));
  }
}

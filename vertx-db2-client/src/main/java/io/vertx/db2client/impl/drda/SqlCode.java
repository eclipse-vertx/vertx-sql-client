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

/**
 * Possible values are documented at:
 * <a href="https://www.ibm.com/support/knowledgecenter/SSEPEK_11.0.0/codes/src/tpc/db2z_n.html">
 * DB2 SQL Codes</a>
 */
public class SqlCode {
	
	public static final int CONNECTION_REFUSED = -4499;
	public static final int RDB_NOT_FOUND = -30061;
	public static final int INVALID_CREDENTIALS = -4214;
	public static final int MISSING_CREDENTIALS = -4461;
	public static final int DATABASE_NOT_FOUND = -1001;
	
	// -104 is a broad error message (illegal symbol encountered in SQL statement) 
	// and could be further broken down by adding more specific SQL error codes and handling them separately 
	public static final int INVALID_SQL_STATEMENT = -104;
	
	public static final int MISMATCHING_COLUMNS_AND_VALUES = -117;
	
	public static final int REPEATED_COLUMN_REFERENCE = -121;
	
	public static final int NO_ORDER_BY_IDENTIFIER = -125;
	
	public static final int UPDATE_READONLY_COLUMN = -151;
	
	public static final int INVALID_KEYWORD = -199;
	
	public static final int AMBIGUOUS_COLUMN_NAME = -203;
	
	// The error message for this says "Object not defined in DB2" in Wikipedia and 
    // "<name> is an undefined name" in the IBM zOS DB2 Knowledge Center
    // But I see it with invalid table names specified in a query
    public static final int OBJECT_NOT_DEFINED = -204;
	
	public static final int COLUMN_DOES_NOT_EXIST = -206;
	
	public static final int NULL_CONSTRAINT_VIOLATION = -407;
	
	public static final int PRIMARY_KEY_CAN_BE_NULL = -542;
	
	public static final int DATA_TYPE_INVALID_ATTR = -604;
	
	public static final int INSERT_INTO_GENERATED_ALWAYS = -798;
	
	public static final int DUPLICATE_KEYS_DETECTED = -803;
	
    private final int code_;

    SqlCode(int code) {
        code_ = code;
    }

    /**
     * Return the SQL code represented by this instance.
     *
     * @return an SQL code
     */
    public final int getCode() {
        return code_;
    }

    public final static SqlCode queuedXAError = new SqlCode(-4203);

    public final static SqlCode disconnectError = new SqlCode(40000);

    /** SQL code for SQL state 02000 (end of data). DRDA does not
     * specify the SQL code for this SQL state, but Derby/DB2 uses 100. */
    public final static SqlCode END_OF_DATA = new SqlCode(100);
    
    @Override
    public String toString() {
      return "SQLCode=" + code_;
    }
}
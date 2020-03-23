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

import java.util.ArrayList;
import java.util.List;

public class ColumnMetaData {
    
    public int columns_;
    
    public boolean[] nullable_;
    
    public short sqldHold_;
    public short sqldReturn_;
    public short sqldScroll_;
    public short sqldSensitive_;
    public short sqldFcode_;
    public short sqldKeytype_;
    public String sqldRdbnam_; // catalog name, not used by driver, placeholder only
    public String sqldSchema_; // schema name, not used by driver, placeholder only
    
    //data comes from SQLDAGRP
    public int[] sqlPrecision_; // adjusted sqllen;
    public int[] sqlScale_;
    public long[] sqlLength_;  // This is maximum length for varchar fields
    /**
     * The DB2 type number. See <code>DRDAConstants#DB2_SQLTYPE_*</code> constants
     */
    public int[] sqlType_;
    public int[] sqlCcsid_;
    
    //Data from SQLDOPTGRP
    public String[] sqlName_;   // column name, pre-allocated
    public String[] sqlLabel_;  // column label
    public short[] sqlUnnamed_;
    public String[] sqlComment_;
    public String[] sqlUDTname_;
    public String[] sqlUDTclassName_;
    
    //Data from SQLDXGRP
    public short[] sqlxKeymem_;
    public short[] sqlxGenerated_;
    public short[] sqlxParmmode_; // pre-allocated
    public String[] sqlxCorname_;
    public String[] sqlxName_;
    public String[] sqlxBasename_;  // table name
    public int[] sqlxUpdatable_;
    public String[] sqlxSchema_;    // schema name
    public String[] sqlxRdbnam_;    // catalog name
    
    // For performance only, not part of logical model.
    public transient int[][] protocolTypesCache_ = null;
    
    /**
     * The client side SQL type. See {@link ClientTypes} constants
     */
    public transient int[] types_;
    public transient int[] clientParamtertype_;

    public void setColumnCount(final int numColumns) {
        if (this.nullable_ != null) {
            if (this.columns_ == numColumns) {
                return;
            } else if (this.columns_ > numColumns) {
                this.columns_ = numColumns;
                return;
            }
        }
        this.columns_ = numColumns;
        this.nullable_ = new boolean[numColumns];
        this.sqlPrecision_ = new int[numColumns];
        this.sqlScale_ = new int[numColumns];
        this.sqlLength_ = new long[numColumns];
        this.sqlType_ = new int[numColumns];
        this.sqlCcsid_ = new int[numColumns];
        this.sqlName_ = new String[numColumns];
        this.sqlLabel_ = new String[numColumns];
        this.sqlUnnamed_ = new short[numColumns];
        this.sqlComment_ = new String[numColumns];
        this.sqlUDTname_ = new String[numColumns];
        this.sqlUDTclassName_ = new String[numColumns];
        this.sqlxKeymem_ = new short[numColumns];
        this.sqlxGenerated_ = new short[numColumns];
        this.sqlxParmmode_ = new short[numColumns];
        this.sqlxCorname_ = new String[numColumns];
        this.sqlxName_ = new String[numColumns];
        this.sqlxBasename_ = new String[numColumns];
        this.sqlxUpdatable_ = new int[numColumns];
        this.sqlxSchema_ = new String[numColumns];
        this.sqlxRdbnam_ = new String[numColumns];
        this.types_ = new int[numColumns];
        this.clientParamtertype_ = new int[numColumns];
    }
    
    public List<String> getColumnNames() {
    	List<String> cols = new ArrayList<>(columns_);
    	for (int i = 0; i < columns_; i++) {
    		cols.add(i, getColumnName(i));
    	}
    	return cols;
    }
    
    public String getColumnName(int i) {
    	if (i < 0)
    		throw new IllegalArgumentException("Requested column name for negative index: " + i);
    	// Prefer column names from SQLDXGRP if set
    	if (sqlxName_ != null && i < sqlxName_.length && sqlxName_[i] != null)
    		return sqlxName_[i];
    	// Otherwise use column names from SQLDOPTGRP
    	if (sqlName_ != null && i < sqlName_.length)
    		return sqlName_[i];
    	return null;
    }
    
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder(super.toString());
    	sb.append("{");
    	for (int i = 0; i < columns_; i++) {
    		sb.append("{name=");
    		sb.append(getColumnName(i));
    		sb.append(", type=");
    		if (types_ != null && i < types_.length) {
    			sb.append(types_[i]);
    			sb.append("/");
    			sb.append(ClientTypes.getTypeString(types_[i]));
    		} else {
    			sb.append("null");
    		}
    		sb.append(", nullable=");
    		if (nullable_ != null && i < nullable_.length) {
    			sb.append(nullable_[i]);
    		} else {
    			sb.append("null");
    		}
    		sb.append("}");
    	}
    	sb.append("}");
    	return sb.toString();
    }
    
}

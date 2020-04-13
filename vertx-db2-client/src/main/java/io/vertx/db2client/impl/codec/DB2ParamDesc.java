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
package io.vertx.db2client.impl.codec;

import io.vertx.db2client.impl.drda.ClientTypes;
import io.vertx.db2client.impl.drda.ColumnMetaData;
import io.vertx.sqlclient.data.Numeric;
import io.vertx.sqlclient.impl.ErrorMessageFactory;
import io.vertx.sqlclient.impl.ParamDesc;
import io.vertx.sqlclient.impl.TupleInternal;

class DB2ParamDesc extends ParamDesc {

    private final ColumnMetaData paramDefinitions;

    DB2ParamDesc(ColumnMetaData paramDefinitions) {
        this.paramDefinitions = paramDefinitions;
    }

    ColumnMetaData paramDefinitions() {
        return paramDefinitions;
    }

    public String prepare(TupleInternal values) {
        if (values.size() != paramDefinitions.columns_) {
        	return ErrorMessageFactory.buildWhenArgumentsLengthNotMatched(paramDefinitions.columns_, values.size());
        }
        for (int i = 0; i < paramDefinitions.columns_; i++) {
        	Object val = values.getValue(i);
        	int type =  paramDefinitions.types_[i];
        	if (!canConvert(val, type)) {
        		return "Parameter at position [" + i + "] with class = [" + val.getClass() + 
        				"] cannot be coerced to [" + ClientTypes.getTypeString(type) + "] for encoding";
        	}
        }
        return null;
    }
    
    private static boolean canConvert(Object val, int type) {
    	if (val == null)
    		return true;
    	if (ClientTypes.canConvert(val, type))
    		return true;
    	Class<?> clazz = val.getClass();
    	switch (type) {
        case ClientTypes.BIGINT:
        case ClientTypes.BOOLEAN:
        case ClientTypes.DECIMAL:
        case ClientTypes.DOUBLE:
        case ClientTypes.INTEGER:
        case ClientTypes.REAL:
        case ClientTypes.SMALLINT:
        	return clazz == Numeric.class;
    	}
    	return false;
    }

}

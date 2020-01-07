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

import io.vertx.db2client.impl.drda.ColumnMetaData;
import io.vertx.sqlclient.Tuple;
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

    @Override
    public String prepare(TupleInternal values) {
        if (values.size() != paramDefinitions.columns_) {
            return buildReport(values);
        }
        // TODO @AGG perform parameter type checking here
        return null;
    }

    private String buildReport(Tuple values) {
        StringBuilder sb = new StringBuilder("Values [");
        for (int i = 0; i < values.size(); i++) {
            sb.append(values.getValue(i));
            if (i < values.size() - 1)
                sb.append(", ");
        }
        sb.append("] cannot be coerced to [");
        for (int i = 0; i < paramDefinitions.columns_; i++) {
            // TODO: @AGG print class name here instead of DB type number
            sb.append(paramDefinitions.types_[i]);
            if (i < values.size() - 1)
                sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}

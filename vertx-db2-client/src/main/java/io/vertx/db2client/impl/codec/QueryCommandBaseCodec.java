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
import io.vertx.sqlclient.impl.command.QueryCommandBase;

abstract class QueryCommandBaseCodec<T, C extends QueryCommandBase<T>> extends CommandCodec<Boolean, C> {

    protected static enum CommandHandlerState {
        HANDLING_COLUMN_DEFINITION, HANDLING_ROW_DATA, HANDLING_END_OF_QUERY
    }

    protected CommandHandlerState commandHandlerState = CommandHandlerState.HANDLING_COLUMN_DEFINITION;
    protected ColumnMetaData columnDefinitions;
    protected RowResultDecoder<?, T> decoder;

    QueryCommandBaseCodec(C cmd) {
        super(cmd);
    }

    @Override
    public String toString() {
        return super.toString() + " sql=" + cmd.sql() + ", autoCommit=" + cmd.autoCommit();
    }
}

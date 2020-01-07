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
package io.vertx.db2client.impl.codec;

import java.util.stream.Collector;

import io.netty.buffer.ByteBuf;
import io.vertx.db2client.impl.drda.CCSIDManager;
import io.vertx.db2client.impl.drda.ColumnMetaData;
import io.vertx.db2client.impl.drda.DRDAQueryRequest;
import io.vertx.db2client.impl.drda.DRDAQueryResponse;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.impl.RowDesc;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.QueryCommandBase;

abstract class QueryCommandBaseCodec<T, C extends QueryCommandBase<T>> extends CommandCodec<Boolean, C> {

    protected static enum CommandHandlerState {
        HANDLING_COLUMN_DEFINITION, HANDLING_ROW_DATA, HANDLING_END_OF_QUERY
    }

    protected CommandHandlerState commandHandlerState = CommandHandlerState.HANDLING_COLUMN_DEFINITION;
    protected ColumnMetaData columnDefinitions;
    protected RowResultDecoder<?, T> decoder;
    CCSIDManager ccsidManager = new CCSIDManager();

    QueryCommandBaseCodec(C cmd) {
        super(cmd);
    }

    private static <A, T> T emptyResult(Collector<Row, A, T> collector) {
        return collector.finisher().apply(collector.supplier().get());
    }

    @Override
    void decodePayload(ByteBuf payload, int payloadLength) {
        if (DRDAQueryRequest.isQuery(cmd.sql()))
            decodeQuery(payload);
        else
            decodeUpdate(payload);
    }

    private void decodeUpdate(ByteBuf payload) {
        System.out.println("@AGG decode update");
        DRDAQueryResponse updateResponse = new DRDAQueryResponse(payload, ccsidManager);
        int updatedCount = (int) updateResponse.readExecuteImmediate();
        // TODO: If auto-generated keys, read an OPNQRY here
        // readOpenQuery()
        updateResponse.readLocalCommit();

        T result = emptyResult(cmd.collector());
        cmd.resultHandler().handleResult(updatedCount, 0, null, result, null);
        completionHandler.handle(CommandResponse.success(true));
    }

    private void decodeQuery(ByteBuf payload) {
        System.out.println("@AGG decode QueryCommandBaseCodec state=" + commandHandlerState);
        switch (commandHandlerState) {
        case HANDLING_COLUMN_DEFINITION:
            DRDAQueryResponse resp = new DRDAQueryResponse(payload, ccsidManager);
            resp.readPrepareDescribeOutput();
            resp.readBeginOpenQuery();
            columnDefinitions = resp.getColumnMetaData();
            decoder = new RowResultDecoder<>(cmd.collector(), new DB2RowDesc(columnDefinitions), resp.getCursor(),
                    resp);
            commandHandlerState = CommandHandlerState.HANDLING_ROW_DATA;
            // return;
            // case HANDLING_ROW_DATA:
            while (decoder.next()) {
                decoder.handleRow(columnDefinitions.columns_, payload);
            }
            if (decoder.isQueryComplete())
                decoder.cursor.setAllRowsReceivedFromServer(true);
            else
                throw new UnsupportedOperationException("Need to fetch more data from DB");
            commandHandlerState = CommandHandlerState.HANDLING_END_OF_QUERY;
            // decodeQuery(payload);
            // return;
            // case HANDLING_END_OF_QUERY:
            int updatedCount = 0; // TODO @AGG hardcoded to 0
            T result;
            Throwable failure;
            int size;
            RowDesc rowDesc;
            failure = decoder.complete();
            result = decoder.result();
            rowDesc = decoder.rowDesc;
            size = decoder.size();
            decoder.reset();
            cmd.resultHandler().handleResult(updatedCount, size, rowDesc, result, failure);
            completionHandler.handle(CommandResponse.success(true));
            return;
        default:
            throw new IllegalStateException("Unknown state: " + commandHandlerState);
        }
    }

    @Override
    public String toString() {
        return super.toString() + " sql=" + cmd.sql();
    }
}

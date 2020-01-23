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

import java.sql.ResultSet;
import java.util.stream.Collector;

import io.netty.buffer.ByteBuf;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.db2client.impl.codec.DB2PreparedStatement.QueryInstance;
import io.vertx.db2client.impl.drda.DRDAQueryRequest;
import io.vertx.db2client.impl.drda.DRDAQueryResponse;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.RowDesc;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;

class ExtendedQueryCommandCodec<R> extends ExtendedQueryCommandBaseCodec<R, ExtendedQueryCommand<R>> {
	
	private static final Logger LOG = LoggerFactory.getLogger(ExtendedQueryCommandCodec.class);
	
	private final QueryInstance queryInstance;
	
    ExtendedQueryCommandCodec(ExtendedQueryCommand<R> cmd) {
        super(cmd);
        columnDefinitions = statement.rowDesc.columnDefinitions();
        queryInstance = statement.getQueryInstance(cmd.cursorId());
    }

    @Override
    void encode(DB2Encoder encoder) {
        super.encode(encoder);
        if (LOG.isDebugEnabled())        
        	LOG.debug("Extended query encode: statement=" + statement);
        
        ByteBuf packet = allocateBuffer();
        DRDAQueryRequest queryRequest = new DRDAQueryRequest(packet);
        String dbName = encoder.socketConnection.database();
        int fetchSize = cmd.fetch(); // TODO @AGG get fetch size from config
        Tuple params = cmd.params();
        Object[] inputs = new Object[params.size()];
        for (int i = 0; i < params.size(); i++)
            inputs[i] = params.getValue(i);
        
        if (DRDAQueryRequest.isQuery(cmd.sql())) {
            if (queryInstance.cursor == null) {
                queryRequest.writeOpenQuery(statement.section, dbName, fetchSize, ResultSet.TYPE_FORWARD_ONLY, inputs.length,
                        statement.paramDesc.paramDefinitions(), inputs);
            } else {
                queryRequest.writeFetch(statement.section, dbName, fetchSize, queryInstance.queryInstanceId);
            }
        } else { // is an update
        	boolean outputExpected = false; // TODO @AGG implement later, is true if result set metadata num columns > 0
        	boolean chainAutoCommit = true;
        	queryRequest.writeExecute(statement.section, dbName, statement.paramDesc.paramDefinitions(), inputs, inputs.length, outputExpected, chainAutoCommit);
        	queryRequest.buildRDBCMM();
        	
        	// TODO: for auto generated keys we also need to flow a writeOpenQuery
        }
        queryRequest.completeCommand();
        encoder.chctx.writeAndFlush(packet);
    }
    
    @Override
    void decodePayload(ByteBuf payload, int payloadLength) {
    	if (LOG.isDebugEnabled())        
        	LOG.debug("Extended query decode");
    	
        if (DRDAQueryRequest.isQuery(cmd.sql())) {
        	decodeQuery(payload);
        } else { // is update
        	decodeUpdate(payload);
        }
    }
    
    private void decodeQuery(ByteBuf payload) {
        DRDAQueryResponse resp = new DRDAQueryResponse(payload);
        if (queryInstance.cursor == null) {
            resp.setOutputColumnMetaData(columnDefinitions);
            resp.readBeginOpenQuery();
            decoder = new RowResultDecoder<>(cmd.collector(), new DB2RowDesc(columnDefinitions), resp.getCursor(), resp);
            queryInstance.cursor = resp.getCursor();
            queryInstance.queryInstanceId = resp.getQueryInstanceId();
            commandHandlerState = CommandHandlerState.HANDLING_ROW_DATA;
        } else {
            resp.readFetch(queryInstance.cursor);
            decoder = new RowResultDecoder<>(cmd.collector(), statement.rowDesc, queryInstance.cursor, resp);
        }
        while (decoder.next()) {
            decoder.handleRow(columnDefinitions.columns_, payload);
        }
        boolean hasMoreResults = !decoder.isQueryComplete();
        if (!hasMoreResults)
            statement.closeQuery(queryInstance);
        
        commandHandlerState = CommandHandlerState.HANDLING_END_OF_QUERY;
        int updatedCount = 0; // @AGG hardcoded to 0
        R result;
        Throwable failure;
        int size;
        RowDesc rowDesc;
        failure = decoder.complete();
        result = decoder.result();
        rowDesc = decoder.rowDesc;
        size = decoder.size();
        decoder.reset();
        cmd.resultHandler().handleResult(updatedCount, size, rowDesc, result, failure);
        completionHandler.handle(CommandResponse.success(hasMoreResults));
        return;
    }
    
    private static <A, T> T emptyResult(Collector<Row, A, T> collector) {
        return collector.finisher().apply(collector.supplier().get());
    }
    
    private void decodeUpdate(ByteBuf payload) {
        DRDAQueryResponse updateResponse = new DRDAQueryResponse(payload);
        int updatedCount = (int) updateResponse.readExecute();
        // TODO: If auto-generated keys, read an OPNQRY here
        // readOpenQuery()
        updateResponse.readLocalCommit();
        statement.closeQuery(queryInstance);

        R result = emptyResult(cmd.collector());
        cmd.resultHandler().handleResult(updatedCount, 0, null, result, null);
        completionHandler.handle(CommandResponse.success(true));
    }
}

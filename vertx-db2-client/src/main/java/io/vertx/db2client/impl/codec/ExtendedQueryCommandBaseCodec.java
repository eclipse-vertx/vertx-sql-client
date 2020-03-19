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
import io.vertx.db2client.impl.codec.DB2PreparedStatement.QueryInstance;
import io.vertx.db2client.impl.drda.DRDAQueryRequest;
import io.vertx.db2client.impl.drda.DRDAQueryResponse;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.data.Numeric;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommandBase;

abstract class ExtendedQueryCommandBaseCodec<R, C extends ExtendedQueryCommandBase<R>>
		extends QueryCommandBaseCodec<R, C> {

	final DB2PreparedStatement statement;

	ExtendedQueryCommandBaseCodec(C cmd) {
		super(cmd);
		statement = (DB2PreparedStatement) cmd.preparedStatement();
		columnDefinitions = statement.rowDesc.columnDefinitions();
	}

	void encodePreparedQuery(DRDAQueryRequest queryRequest, QueryInstance queryInstance, Tuple params) {
	    int requiredParams = statement.paramDesc.paramDefinitions().columns_;
    	if (params.size() != requiredParams) {
    		completionHandler.handle(CommandResponse.failure("Only " + params.size() + " prepared statement parameters were provided " +
    				"but " + requiredParams + " parameters are required."));
    		return;
    	}
	    
		Object[] inputs = sanitize(params);
		if (queryInstance.cursor == null) {
			queryRequest.writeOpenQuery(statement.section, encoder.socketConnection.database(), cmd.fetch(),
					ResultSet.TYPE_FORWARD_ONLY, statement.paramDesc.paramDefinitions(), inputs);
		} else {
			queryRequest.writeFetch(statement.section, encoder.socketConnection.database(), cmd.fetch(),
					queryInstance.queryInstanceId);
		}
	}

	void encodePreparedUpdate(DRDAQueryRequest queryRequest, Tuple params) {
		Object[] inputs = sanitize(params);
		boolean outputExpected = false; // TODO @AGG implement later, is true if result set metadata num columns > 0
		boolean chainAutoCommit = true;
		queryRequest.writeExecute(statement.section, encoder.socketConnection.database(),
				statement.paramDesc.paramDefinitions(), inputs, outputExpected, chainAutoCommit);
		// TODO: for auto generated keys we also need to flow a writeOpenQuery
	}

	RowResultDecoder<?, R> decodePreparedQuery(ByteBuf payload, DRDAQueryResponse resp, QueryInstance queryInstance) {
		RowResultDecoder<?, R> decoder = null;
		if (queryInstance.cursor == null) {
			resp.setOutputColumnMetaData(columnDefinitions);
			resp.readBeginOpenQuery();
			decoder = new RowResultDecoder<>(cmd.collector(), new DB2RowDesc(columnDefinitions), resp.getCursor(), resp);
			queryInstance.cursor = resp.getCursor();
			queryInstance.queryInstanceId = resp.getQueryInstanceId();
		} else {
			resp.readFetch(queryInstance.cursor);
			decoder = new RowResultDecoder<>(cmd.collector(), statement.rowDesc, queryInstance.cursor, resp);
		}
		while (decoder.next()) {
			decoder.handleRow(columnDefinitions.columns_, payload);
		}
		return decoder;
	}

	void handleUpdateResult(DRDAQueryResponse updateResponse) {
		int updatedCount = (int) updateResponse.readExecute();
		// TODO: If auto-generated keys, read an OPNQRY here
		// readOpenQuery()
		R result = emptyResult(cmd.collector());
		cmd.resultHandler().handleResult(updatedCount, 0, null, result, null);
	}

	static <A, T> T emptyResult(Collector<Row, A, T> collector) {
		return collector.finisher().apply(collector.supplier().get());
	}
	
	private static Object[] sanitize(Tuple params) {
		Object[] inputs = new Object[params.size()];
		for (int i = 0; i < params.size(); i++) {
			Object val = params.getValue(i);
			if (val instanceof Numeric)
				val = ((Numeric) val).bigDecimalValue();
			inputs[i] = val;
		}
		return inputs;
	}
	
}

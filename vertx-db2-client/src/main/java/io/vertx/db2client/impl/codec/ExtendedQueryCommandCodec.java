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

import io.netty.buffer.ByteBuf;
import io.vertx.db2client.impl.codec.DB2PreparedStatement.QueryInstance;
import io.vertx.db2client.impl.drda.DRDAQueryRequest;
import io.vertx.db2client.impl.drda.DRDAQueryResponse;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;

class ExtendedQueryCommandCodec<R> extends ExtendedQueryCommandBaseCodec<R, ExtendedQueryCommand<R>> {
  
    final QueryInstance queryInstance;
	
    ExtendedQueryCommandCodec(ExtendedQueryCommand<R> cmd) {
        super(cmd);
        queryInstance = statement.getQueryInstance(cmd.cursorId());
    }
    
    @Override
    void encodeQuery(DRDAQueryRequest req) {
    	encodePreparedQuery(req, queryInstance, cmd.params());
    }
    
    @Override
    void encodeUpdate(DRDAQueryRequest req) {
    	encodePreparedUpdate(req, cmd.params());
    	if (cmd.autoCommit()) {
      	  req.buildRDBCMM();
      	}
    }

    void decodeQuery(ByteBuf payload) {
        DRDAQueryResponse resp = new DRDAQueryResponse(payload);
        RowResultDecoder<?, R> decoder = decodePreparedQuery(payload, resp, queryInstance);
        boolean hasMoreResults = !decoder.isQueryComplete();
        handleQueryResult(decoder);
        completionHandler.handle(CommandResponse.success(hasMoreResults));
    }
    
    void decodeUpdate(ByteBuf payload) {
        DRDAQueryResponse updateResponse = new DRDAQueryResponse(payload);
        handleUpdateResult(updateResponse);
        if (cmd.autoCommit()) {
          updateResponse.readLocalCommit();
        }
        completionHandler.handle(CommandResponse.success(true));
    }
    
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());
		sb.append(", params=");
		sb.append(cmd.params());
		return sb.toString();
	}
}

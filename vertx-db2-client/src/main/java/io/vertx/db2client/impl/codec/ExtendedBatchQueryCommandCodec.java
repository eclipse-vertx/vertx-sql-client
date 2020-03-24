package io.vertx.db2client.impl.codec;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.db2client.impl.codec.DB2PreparedStatement.QueryInstance;
import io.vertx.db2client.impl.drda.DRDAQueryRequest;
import io.vertx.db2client.impl.drda.DRDAQueryResponse;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.ExtendedBatchQueryCommand;

class ExtendedBatchQueryCommandCodec<R> extends ExtendedQueryCommandBaseCodec<R, ExtendedBatchQueryCommand<R>> {
  
  private static final Logger LOG = LoggerFactory.getLogger(ExtendedBatchQueryCommandCodec.class);

  private final List<Tuple> params;
  private final List<QueryInstance> queryInstances;
  private final String baseCursorId;

  ExtendedBatchQueryCommandCodec(ExtendedBatchQueryCommand<R> cmd) {
    super(cmd);
    params = cmd.params();
    queryInstances = new ArrayList<>(params.size());
    baseCursorId = (cmd.cursorId() == null ? UUID.randomUUID().toString() : cmd.cursorId()) + "-";
  }

  @Override
  void encode(DB2Encoder encoder) {
    if (params.isEmpty()) {
      completionHandler.handle(CommandResponse.failure("Can not execute batch query with 0 sets of batch parameters."));
      return;
    }
    
    super.encode(encoder);
  }
  
	@Override
	void encodeQuery(DRDAQueryRequest req) {
		for (int i = 0; i < params.size(); i++) {
		      Tuple params = this.params.get(i);
		      QueryInstance queryInstance = statement.getQueryInstance(baseCursorId + i);
		      queryInstances.add(i, queryInstance);
		      encodePreparedQuery(req, queryInstance, params);
		}
	}

	@Override
	void encodeUpdate(DRDAQueryRequest req) {
		for (Tuple params : this.params) {
			encodePreparedUpdate(req, params);
		}
	    if (cmd.autoCommit()) {
	        req.buildRDBCMM();
	    }
	}
  
  void decodeQuery(ByteBuf payload) {
      boolean hasMoreResults = true;
      DRDAQueryResponse resp = new DRDAQueryResponse(payload, encoder.socketConnection.dbMetadata);
      for (int i = 0; i < params.size(); i++) {
        if (LOG.isDebugEnabled())
          LOG.debug("Decode query " + i);
        QueryInstance queryInstance = queryInstances.get(i);
        RowResultDecoder<?, R> decoder = decodePreparedQuery(payload, resp, queryInstance);
        boolean queryComplete = decoder.isQueryComplete();
        if (queryComplete) {
          resp.readEndOpenQuery();
          statement.closeQuery(queryInstance);
        }
        
        hasMoreResults &= !queryComplete;
        handleQueryResult(decoder);
      }
      completionHandler.handle(CommandResponse.success(hasMoreResults));
  }
  
  void decodeUpdate(ByteBuf payload) {
      DRDAQueryResponse updateResponse = new DRDAQueryResponse(payload, encoder.socketConnection.dbMetadata);
      for (int i = 0; i < params.size(); i++) {
    	  handleUpdateResult(updateResponse);
      }
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

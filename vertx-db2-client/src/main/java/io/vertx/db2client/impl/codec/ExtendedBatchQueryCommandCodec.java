package io.vertx.db2client.impl.codec;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
    super.encode(encoder);
    if (params.isEmpty() && statement.paramDesc.paramDefinitions().columns_ > 0) {
      completionHandler.handle(CommandResponse.failure("Statement parameter is not set because of the empty batch param list"));
      return;
    }
    
    // TODO: Move common bits to base class
    ByteBuf packet = allocateBuffer();
    DRDAQueryRequest queryRequest = new DRDAQueryRequest(packet);
    if (DRDAQueryRequest.isQuery(cmd.sql())) {
      encodeBatchQuery(queryRequest);
    } else {
      encodeBatchUpdate(queryRequest);
    }
    queryRequest.completeCommand();
    encoder.chctx.writeAndFlush(packet);
  }
  
  private void encodeBatchQuery(DRDAQueryRequest queryRequest) {
    for (int i = 0; i < params.size(); i++) {
      Tuple params = this.params.get(i);
      QueryInstance queryInstance = statement.getQueryInstance(baseCursorId + i);
      queryInstances.add(i, queryInstance);
      Object[] inputs = new Object[params.size()];
      for (int j = 0; j < params.size(); j++)
          inputs[j] = params.getValue(j);
      if (queryInstance.cursor == null) {
          queryRequest.writeOpenQuery(statement.section, encoder.socketConnection.database(), cmd.fetch(), ResultSet.TYPE_FORWARD_ONLY,
                                      inputs.length, statement.paramDesc.paramDefinitions(), inputs);
      } else {
          queryRequest.writeFetch(statement.section, encoder.socketConnection.database(), cmd.fetch(), queryInstance.queryInstanceId);
      }
    }
  }
  
  private void encodeBatchUpdate(DRDAQueryRequest queryRequest) {
    for (Tuple params : this.params) {
      Object[] inputs = new Object[params.size()];
      for (int i = 0; i < params.size(); i++)
        inputs[i] = params.getValue(i);
      boolean outputExpected = false; // TODO @AGG implement later, is true if result set metadata num columns > 0
      boolean chainAutoCommit = true;
      queryRequest.writeExecute(statement.section, encoder.socketConnection.database(), statement.paramDesc.paramDefinitions(), 
          inputs, inputs.length, outputExpected, chainAutoCommit);
      // TODO: for auto generated keys we also need to flow a writeOpenQuery
    }
    if (cmd.autoCommit()) {
      queryRequest.buildRDBCMM();
    }
  }
  
  @Override
  void decodePayload(ByteBuf payload, int payloadLength) {
      if (DRDAQueryRequest.isQuery(cmd.sql())) {
          decodeQuery(payload);
      } else {
          decodeUpdate(payload);
      }
  }
  
  private void decodeQuery(ByteBuf payload) {
      boolean hasMoreResults = true;
      DRDAQueryResponse resp = new DRDAQueryResponse(payload);
      for (int i = 0; i < params.size(); i++) {
        if (LOG.isDebugEnabled())
          LOG.debug("Decode query " + i);
        RowResultDecoder<?, R> decoder = null;
        QueryInstance queryInstance = queryInstances.get(i);
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
        boolean queryComplete = decoder.isQueryComplete();
        if (queryComplete) {
          resp.readEndOpenQuery();
          statement.closeQuery(queryInstance);
        }
        
        hasMoreResults &= !queryComplete;
        commandHandlerState = CommandHandlerState.HANDLING_END_OF_QUERY;
        
        Throwable failure = decoder.complete();
        R result = decoder.result();
        RowDesc rowDesc = decoder.rowDesc;
        int size = decoder.size();
        int updatedCount = decoder.size();
        decoder.reset();
        cmd.resultHandler().handleResult(updatedCount, size, rowDesc, result, failure);
      }
      completionHandler.handle(CommandResponse.success(hasMoreResults));
      return;
  }
  
  private static <A, T> T emptyResult(Collector<Row, A, T> collector) {
      return collector.finisher().apply(collector.supplier().get());
  }
  
  private void decodeUpdate(ByteBuf payload) {
      DRDAQueryResponse updateResponse = new DRDAQueryResponse(payload);
      for (int i = 0; i < params.size(); i++) {
        int updatedCount = (int) updateResponse.readExecute();
        // TODO: If auto-generated keys, read an OPNQRY here
        // readOpenQuery()
        R result = emptyResult(cmd.collector());
        cmd.resultHandler().handleResult(updatedCount, 0, null, result, null);
      }
      if (cmd.autoCommit()) {
        updateResponse.readLocalCommit();
      }
      completionHandler.handle(CommandResponse.success(true));
  }

}

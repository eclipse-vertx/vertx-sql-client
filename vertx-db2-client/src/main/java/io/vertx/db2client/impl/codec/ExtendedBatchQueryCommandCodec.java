/*
 * Copyright (C) 2020 IBM Corporation
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import io.netty.buffer.ByteBuf;
import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.db2client.impl.codec.DB2PreparedStatement.QueryInstance;
import io.vertx.db2client.impl.drda.DRDAQueryRequest;
import io.vertx.db2client.impl.drda.DRDAQueryResponse;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.internal.TupleInternal;
import io.vertx.sqlclient.impl.connection.CommandResponse;
import io.vertx.sqlclient.spi.protocol.ExtendedQueryCommand;

public class ExtendedBatchQueryCommandCodec<R> extends ExtendedQueryCommandBaseCodec<R, ExtendedQueryCommand<R>> {

  private static final Logger LOG = LoggerFactory.getLogger(ExtendedBatchQueryCommandCodec.class);

  private final List<TupleInternal> params;
  private final List<QueryInstance> queryInstances;
  private final String baseCursorId;

  public ExtendedBatchQueryCommandCodec(ExtendedQueryCommand<R> cmd, DB2PreparedStatement statement) {
    super(cmd, statement);
    params = cmd.paramsList();
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
    DRDAQueryResponse resp = new DRDAQueryResponse(payload, encoder.socketConnection.connMetadata);
    for (int i = 0; i < params.size(); i++) {
      if (LOG.isDebugEnabled())
        LOG.debug("Decode query " + i);
      QueryInstance queryInstance = queryInstances.get(i);
      RowResultDecoder<?, R> decoder = decodePreparedQuery(payload, resp, queryInstance);
      boolean queryComplete = decoder.isQueryComplete();
      hasMoreResults &= !queryComplete;
      handleQueryResult(decoder);
    }
    completionHandler.handle(CommandResponse.success(hasMoreResults));
  }

  void decodeUpdate(ByteBuf payload) {
    DRDAQueryResponse updateResponse = new DRDAQueryResponse(payload, encoder.socketConnection.connMetadata);
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
    sb.append("[");
    sb.append(cmd.paramsList().stream().map(Tuple::deepToString).collect(Collectors.joining(",")));
    sb.append("]");
    return sb.toString();
  }

}

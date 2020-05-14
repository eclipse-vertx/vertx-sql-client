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
import io.vertx.db2client.impl.drda.DRDAQueryRequest;
import io.vertx.db2client.impl.drda.DRDAQueryResponse;
import io.vertx.db2client.impl.drda.Section;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.SimpleQueryCommand;

class SimpleQueryCommandCodec<T> extends QueryCommandBaseCodec<T, SimpleQueryCommand<T>> {

  private Section querySection;

  SimpleQueryCommandCodec(SimpleQueryCommand<T> cmd) {
    super(cmd);
  }

  @Override
  void encodeQuery(DRDAQueryRequest queryCommand) {
    querySection = encoder.connMetadata.sectionManager.getSection(cmd.sql());
    queryCommand.writePrepareDescribeOutput(cmd.sql(), encoder.connMetadata.databaseName, querySection);
    // fetchSize=0 triggers default fetch size (64) to be used (TODO @AGG this
    // should be configurable)
    // @AGG hard coded to TYPE_FORWARD_ONLY
    queryCommand.writeOpenQuery(querySection, encoder.connMetadata.databaseName, 0, ResultSet.TYPE_FORWARD_ONLY);
  }

  @Override
  void encodeUpdate(DRDAQueryRequest updateCommand) {
    querySection = encoder.connMetadata.sectionManager.getSection(cmd.sql());
    updateCommand.writeExecuteImmediate(cmd.sql(), querySection, encoder.connMetadata.databaseName);
    if (cmd.autoCommit()) {
      updateCommand.buildRDBCMM();
    }

    // @AGG TODO: auto-generated keys chain an OPNQRY command
    // updateCommand.writeOpenQuery(s,
    // encoder.dbMetadata.databaseName,
    // 0, // triggers default fetch size (64) to be used @AGG this should be
    // configurable
    // ResultSet.TYPE_FORWARD_ONLY); // @AGG hard code to TYPE_FORWARD_ONLY
  }

  void decodeUpdate(ByteBuf payload) {
    DRDAQueryResponse updateResponse = new DRDAQueryResponse(payload, encoder.connMetadata);
    querySection.release();

    int updatedCount = (int) updateResponse.readExecuteImmediate();
    // TODO: If auto-generated keys, read an OPNQRY here
    // readOpenQuery()
    T result = emptyResult(cmd.collector());
    cmd.resultHandler().handleResult(updatedCount, 0, null, result, null);

    if (cmd.autoCommit()) {
      updateResponse.readLocalCommit();
    }
    completionHandler.handle(CommandResponse.success(true));
  }

  static <A, T> T emptyResult(Collector<Row, A, T> collector) {
    return collector.finisher().apply(collector.supplier().get());
  }

  void decodeQuery(ByteBuf payload) {
    querySection.release();

    DRDAQueryResponse resp = new DRDAQueryResponse(payload, encoder.connMetadata);
    resp.readPrepareDescribeOutput();
    resp.readBeginOpenQuery();
    columnDefinitions = resp.getOutputColumnMetaData();
    RowResultDecoder<?, T> decoder = new RowResultDecoder<>(cmd.collector(), new DB2RowDesc(columnDefinitions),
        resp.getCursor(), resp);

    while (decoder.next()) {
      decoder.handleRow(columnDefinitions.columns_, payload);
    }
    if (decoder.isQueryComplete()) {
      decoder.cursor.setAllRowsReceivedFromServer(true);
    } else {
      throw new UnsupportedOperationException("Need to fetch more data from DB");
    }

    handleQueryResult(decoder);
    completionHandler.handle(CommandResponse.success(true));
  }

  @Override
  public String toString() {
    return super.toString() + ", section=" + querySection;
  }

}

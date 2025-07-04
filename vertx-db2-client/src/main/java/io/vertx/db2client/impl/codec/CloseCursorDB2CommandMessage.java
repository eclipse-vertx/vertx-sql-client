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

import io.netty.buffer.ByteBuf;
import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.db2client.impl.codec.DB2PreparedStatement.QueryInstance;
import io.vertx.db2client.impl.drda.DRDAQueryRequest;
import io.vertx.db2client.impl.drda.DRDAQueryResponse;
import io.vertx.sqlclient.spi.protocol.CloseCursorCommand;
import io.vertx.sqlclient.codec.CommandResponse;

class CloseCursorDB2CommandMessage extends DB2CommandMessage<Void, CloseCursorCommand> {

  private static final Logger LOG = LoggerFactory.getLogger(CloseCursorDB2CommandMessage.class);

  CloseCursorDB2CommandMessage(CloseCursorCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(DB2Encoder encoder) {
    super.encode(encoder);
    DB2PreparedStatement statement = (DB2PreparedStatement) cmd.statement();
    if (LOG.isDebugEnabled())
      LOG.debug("Close cursor with id=" + cmd.id());
    QueryInstance query = statement.getQueryInstance(cmd.id());
    statement.closeQuery(query);

    ByteBuf packet = allocateBuffer();
    DRDAQueryRequest closeCursor = new DRDAQueryRequest(packet, encoder.socketConnection.connMetadata);
    closeCursor.buildCLSQRY(statement.section, encoder.socketConnection.connMetadata.databaseName, query.queryInstanceId);
    closeCursor.completeCommand();
    sendNonSplitPacket(packet);
  }

  @Override
  void decodePayload(ByteBuf payload, int payloadLength) {
    DRDAQueryResponse closeCursor = new DRDAQueryResponse(payload, encoder.socketConnection.connMetadata);
    closeCursor.readCursorClose();
    fireCommandSuccess(null);
  }
}

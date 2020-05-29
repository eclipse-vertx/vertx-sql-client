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
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.db2client.impl.drda.ColumnMetaData;
import io.vertx.db2client.impl.drda.DRDAQueryRequest;
import io.vertx.db2client.impl.drda.DRDAQueryResponse;
import io.vertx.db2client.impl.drda.Section;
import io.vertx.sqlclient.impl.PreparedStatement;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.PrepareStatementCommand;

class PrepareStatementCodec extends CommandCodec<PreparedStatement, PrepareStatementCommand> {

  private static final Logger LOG = LoggerFactory.getLogger(PrepareStatementCodec.class);

  private static enum CommandHandlerState {
    INIT, HANDLING_PARAM_COLUMN_DEFINITION, 
    PARAM_DEFINITIONS_DECODING_COMPLETED, 
    HANDLING_COLUMN_COLUMN_DEFINITION,
    COLUMN_DEFINITIONS_DECODING_COMPLETED
  }

  private CommandHandlerState commandHandlerState = CommandHandlerState.INIT;
  private ColumnMetaData paramDesc;
  private ColumnMetaData rowDesc;
  private Section section;

  PrepareStatementCodec(PrepareStatementCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(DB2Encoder encoder) {
    super.encode(encoder);
    sendStatementPrepareCommand();
  }

  private void sendStatementPrepareCommand() {
    ByteBuf packet = allocateBuffer();
    // encode packet header
    int packetStartIdx = packet.writerIndex();
    DRDAQueryRequest prepareCommand = new DRDAQueryRequest(packet, encoder.socketConnection.connMetadata);
    section = encoder.socketConnection.connMetadata.sectionManager.getSection(cmd.sql());
    String dbName = encoder.socketConnection.connMetadata.databaseName;
    prepareCommand.writePrepareDescribeOutput(cmd.sql(), dbName, section);
    prepareCommand.writeDescribeInput(section, dbName);
    prepareCommand.completeCommand();

    // set payload length
    int payloadLength = packet.writerIndex() - packetStartIdx;
    sendPacket(packet, payloadLength);
  }

  @Override
  void decodePayload(ByteBuf payload, int payloadLength) {
    switch (commandHandlerState) {
    case INIT:
      DRDAQueryResponse response = new DRDAQueryResponse(payload, encoder.socketConnection.connMetadata);
      response.readPrepareDescribeInputOutput();
      rowDesc = response.getOutputColumnMetaData();
      paramDesc = response.getInputColumnMetaData();
      if (LOG.isDebugEnabled()) {
        LOG.debug("Prepared parameters: " + paramDesc);
      }
      handleColumnDefinitionsDecodingCompleted();
      commandHandlerState = CommandHandlerState.COLUMN_DEFINITIONS_DECODING_COMPLETED;
      break;
    default:
      throw new IllegalStateException("Unknown state: " + commandHandlerState);
    }
  }

  private void handleReadyForQuery() {
    completionHandler.handle(CommandResponse.success(new DB2PreparedStatement(cmd.sql(), new DB2ParamDesc(paramDesc),
        new DB2RowDesc(rowDesc), section, cmd.cacheable())));
  }

  private void resetIntermediaryResult() {
    commandHandlerState = CommandHandlerState.INIT;
    rowDesc = null;
    paramDesc = null;
    section = null;
  }

  private void handleColumnDefinitionsDecodingCompleted() {
    handleReadyForQuery();
    resetIntermediaryResult();
  }

  @Override
  public String toString() {
    return new StringBuilder(getClass().getSimpleName()).append("@").append(Integer.toHexString(hashCode()))
        .append(" sql=").append(cmd.sql()).append(", section=").append(section).toString();
  }

}

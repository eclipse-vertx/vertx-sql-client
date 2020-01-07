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

import io.netty.buffer.ByteBuf;
import io.vertx.db2client.impl.drda.CCSIDManager;
import io.vertx.db2client.impl.drda.ColumnMetaData;
import io.vertx.db2client.impl.drda.DRDAQueryRequest;
import io.vertx.db2client.impl.drda.DRDAQueryResponse;
import io.vertx.db2client.impl.drda.Section;
import io.vertx.db2client.impl.drda.SectionManager;
import io.vertx.sqlclient.impl.PreparedStatement;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.PrepareStatementCommand;

class PrepareStatementCodec extends CommandCodec<PreparedStatement, PrepareStatementCommand> {

    private static enum CommandHandlerState {
        INIT, 
        HANDLING_PARAM_COLUMN_DEFINITION, 
        PARAM_DEFINITIONS_DECODING_COMPLETED, 
        HANDLING_COLUMN_COLUMN_DEFINITION, 
        COLUMN_DEFINITIONS_DECODING_COMPLETED
    }

    private CommandHandlerState commandHandlerState = CommandHandlerState.INIT;
    private ColumnMetaData columnDescs;
    private final CCSIDManager ccsidManager = new CCSIDManager();
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
        System.out.println("@AGG PS encode");
        ByteBuf packet = allocateBuffer();
        // encode packet header
        int packetStartIdx = packet.writerIndex();
        DRDAQueryRequest prepareCommand = new DRDAQueryRequest(packet, ccsidManager);
        section = SectionManager.INSTANCE.getDynamicSection();
        String dbName = encoder.socketConnection.database();
        prepareCommand.writePrepareDescribeOutput(cmd.sql(), dbName, section);
        prepareCommand.writeDescribeInput(section, dbName);
        prepareCommand.completeCommand();

        // set payload length
        int payloadLength = packet.writerIndex() - packetStartIdx;
        sendPacket(packet, payloadLength);
    }

    @Override
    void decodePayload(ByteBuf payload, int payloadLength) {
        System.out.println("@AGG inside PS decode");
        switch (commandHandlerState) {
        case INIT:
            DRDAQueryResponse response = new DRDAQueryResponse(payload, ccsidManager);
            response.readPrepareDescribeInputOutput();
            ColumnMetaData columnMd = response.getColumnMetaData();
            columnDescs = columnMd;
            handleColumnDefinitionsDecodingCompleted();
            commandHandlerState = CommandHandlerState.COLUMN_DEFINITIONS_DECODING_COMPLETED;
            break;
        default:
            throw new IllegalStateException("Unknown state: " + commandHandlerState);
        }
    }

    private void handleReadyForQuery() {
        completionHandler.handle(CommandResponse.success(new DB2PreparedStatement(cmd.sql(),
                new DB2ParamDesc(columnDescs), new DB2RowDesc(columnDescs), section)));
    }

    private void resetIntermediaryResult() {
        commandHandlerState = CommandHandlerState.INIT;
        columnDescs = null;
    }

    private void handleColumnDefinitionsDecodingCompleted() {
        handleReadyForQuery();
        resetIntermediaryResult();
    }

}

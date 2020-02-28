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
import io.vertx.db2client.impl.drda.SectionManager;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.impl.RowDesc;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.SimpleQueryCommand;

class SimpleQueryCommandCodec<T> extends QueryCommandBaseCodec<T, SimpleQueryCommand<T>> {
    
    private Section querySection;

    SimpleQueryCommandCodec(SimpleQueryCommand<T> cmd) {
        super(cmd);
    }

    @Override
    void encode(DB2Encoder encoder) {
        super.encode(encoder);
        try {
        	querySection = SectionManager.INSTANCE.getDynamicSection();
            if (DRDAQueryRequest.isQuery(cmd.sql()))
                sendQueryCommand();
            else
                sendUpdateCommand();
        } catch (Throwable t) {
            t.printStackTrace();
            completionHandler.handle(CommandResponse.failure(t));
        }
    }

    private void sendUpdateCommand() {
        ByteBuf packet = allocateBuffer();
        int packetStartIdx = packet.writerIndex();
        DRDAQueryRequest updateCommand = new DRDAQueryRequest(packet);
        updateCommand.writeExecuteImmediate(cmd.sql(), querySection, encoder.socketConnection.database());
        if (cmd.autoCommit()) {
          updateCommand.buildRDBCMM();
        }
        updateCommand.completeCommand();

        // @AGG TODO: auto-generated keys chain an OPNQRY command
        // updateCommand.writeOpenQuery(s,
        // encoder.socketConnection.database(),
        // 0, // triggers default fetch size (64) to be used @AGG this should be
        // configurable
        // ResultSet.TYPE_FORWARD_ONLY); // @AGG hard code to TYPE_FORWARD_ONLY

        sendPacket(packet, packet.writerIndex() - packetStartIdx);
    }

    private void sendQueryCommand() {
        ByteBuf packet = allocateBuffer();
        int packetStartIdx = packet.writerIndex();

        DRDAQueryRequest queryCommand = new DRDAQueryRequest(packet);
        queryCommand.writePrepareDescribeOutput(cmd.sql(), encoder.socketConnection.database(), querySection);
        // fetchSize=0 triggers default fetch size (64) to be used (TODO @AGG this should be configurable)
        // @AGG hard coded to TYPE_FORWARD_ONLY
        queryCommand.writeOpenQuery(querySection, encoder.socketConnection.database(), 0, 
                ResultSet.TYPE_FORWARD_ONLY);
        queryCommand.completeCommand();

        sendPacket(packet, packet.writerIndex() - packetStartIdx);
    }
    
    static <A, T> T emptyResult(Collector<Row, A, T> collector) {
        return collector.finisher().apply(collector.supplier().get());
    }

    @Override
    void decodePayload(ByteBuf payload, int payloadLength) {
        if (DRDAQueryRequest.isQuery(cmd.sql()))
            decodeQuery(payload);
        else
            decodeUpdate(payload);
    }

    private void decodeUpdate(ByteBuf payload) {
        querySection.release();
        
        DRDAQueryResponse updateResponse = new DRDAQueryResponse(payload);
        int updatedCount = (int) updateResponse.readExecuteImmediate();
        // TODO: If auto-generated keys, read an OPNQRY here
        // readOpenQuery()
        if (cmd.autoCommit()) {
          updateResponse.readLocalCommit();
        }

        T result = emptyResult(cmd.collector());
        cmd.resultHandler().handleResult(updatedCount, 0, null, result, null);
        completionHandler.handle(CommandResponse.success(true));
    }

    private void decodeQuery(ByteBuf payload) {
        switch (commandHandlerState) {
        case HANDLING_COLUMN_DEFINITION:
            DRDAQueryResponse resp = new DRDAQueryResponse(payload);
            resp.readPrepareDescribeOutput();
            resp.readBeginOpenQuery();
            columnDefinitions = resp.getOutputColumnMetaData();
            decoder = new RowResultDecoder<>(cmd.collector(), new DB2RowDesc(columnDefinitions), resp.getCursor(),
                    resp);
            commandHandlerState = CommandHandlerState.HANDLING_ROW_DATA;
            // return;
            // case HANDLING_ROW_DATA:
            while (decoder.next()) {
                decoder.handleRow(columnDefinitions.columns_, payload);
            }
            if (decoder.isQueryComplete())
                decoder.cursor.setAllRowsReceivedFromServer(true);
            else
                throw new UnsupportedOperationException("Need to fetch more data from DB");
            
            commandHandlerState = CommandHandlerState.HANDLING_END_OF_QUERY;
            querySection.release();
            // decodeQuery(payload);
            // return;
            // case HANDLING_END_OF_QUERY:
            Throwable failure = decoder.complete();
            T result = decoder.result();
            RowDesc rowDesc = decoder.rowDesc;
            int size = decoder.size();
            int updatedCount = decoder.size();
            decoder.reset();
            cmd.resultHandler().handleResult(updatedCount, size, rowDesc, result, failure);
            completionHandler.handle(CommandResponse.success(true));
            return;
        default:
            throw new IllegalStateException("Unknown state: " + commandHandlerState);
        }
    }

}

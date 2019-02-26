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
package io.reactiverse.pgclient.impl.my.codec;

import io.netty.buffer.ByteBuf;
import io.reactiverse.mysqlclient.impl.protocol.CommandType;
import io.reactiverse.mysqlclient.impl.protocol.backend.ColumnDefinition;
import io.reactiverse.pgclient.impl.PreparedStatement;
import io.reactiverse.pgclient.impl.ParamDesc;
import io.reactiverse.pgclient.impl.command.CommandResponse;
import io.reactiverse.pgclient.impl.command.PrepareStatementCommand;

import java.nio.charset.StandardCharsets;

import static io.reactiverse.mysqlclient.impl.protocol.backend.ErrPacket.ERROR_PACKET_HEADER;

public class PrepareStatementCodec extends CommandCodec<PreparedStatement, PrepareStatementCommand> {

  private enum CommandHandlerState {
    INIT,
    HANDLING_PARAM_COLUMN_DEFINITION,
    HANDLING_COLUMN_COLUMN_DEFINITION
  }

  private CommandHandlerState commandHandlerState = CommandHandlerState.INIT;
  private long statementId;
  private int processingIndex;
  private ColumnDefinition[] paramDescs;
  private MyColumnDesc[] columnDescs;

  PrepareStatementCodec(PrepareStatementCommand cmd) {
    super(cmd);
  }

  @Override
  void encodePayload(MyEncoder encoder) {
    super.encodePayload(encoder);
    ByteBuf payload = encoder.chctx.alloc().ioBuffer();
    payload.writeByte(CommandType.COM_STMT_PREPARE);
    payload.writeCharSequence(cmd.sql(), StandardCharsets.UTF_8);
    encoder.writePacketAndFlush(sequenceId++, payload);
  }

  @Override
  void decodePayload(ByteBuf payload, MyEncoder encoder, int payloadLength, int sequenceId) {
    switch (commandHandlerState) {
      case INIT:
        int firstByte = payload.getUnsignedByte(payload.readerIndex());
        if (firstByte == ERROR_PACKET_HEADER) {
          // handleErrorPacketPayload(ctx, payload);
          throw new UnsupportedOperationException();
        } else {
          // handle COM_STMT_PREPARE response
          payload.readUnsignedByte(); // 0x00: OK
          long statementId = payload.readUnsignedIntLE();
          int numberOfColumns = payload.readUnsignedShortLE();
          int numberOfParameters = payload.readUnsignedShortLE();
          payload.readByte(); // [00] filler
          int numberOfWarnings = payload.readShortLE();

          // handle metadata here
          this.statementId = statementId;
          this.columnDescs = new MyColumnDesc[numberOfColumns];
          this.paramDescs = new ColumnDefinition[numberOfParameters];
          this.commandHandlerState = CommandHandlerState.HANDLING_PARAM_COLUMN_DEFINITION;
        }
        break;
      case HANDLING_PARAM_COLUMN_DEFINITION:
        paramDescs[processingIndex++] = decodeColumnDefinitionPacketPayload(payload);
        if (processingIndex == paramDescs.length) {
          this.processingIndex = 0;
          this.commandHandlerState = CommandHandlerState.HANDLING_COLUMN_COLUMN_DEFINITION;
        }
        break;
      case HANDLING_COLUMN_COLUMN_DEFINITION:
        columnDescs[processingIndex++] = new MyColumnDesc(decodeColumnDefinitionPacketPayload(payload));
        if (processingIndex == columnDescs.length) {
//          preparedStatement.columnMetadata = new ColumnMetadata(columnDefinitions, DataFormat.BINARY);
//          ctx.fireChannelRead(MySQLCommandResponse.success(preparedStatement));
//          resetIntermediaryResult();
          // NOT THE RIGHT TYPE
          completionHandler.handle(CommandResponse.success(new MyPreparedStatement(
            cmd.sql(),
            statementId,
            paramDescs,
            new ParamDesc(),
            new MyRowDesc(columnDescs))));
        }
        break;
    }
  }
}

package io.reactiverse.mysqlclient.impl.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.reactiverse.mysqlclient.impl.ColumnMetadata;
import io.reactiverse.mysqlclient.impl.MySQLExceptionFactory;
import io.reactiverse.mysqlclient.impl.MySQLPreparedStatement;
import io.reactiverse.mysqlclient.impl.MySQLSocketConnection;
import io.reactiverse.mysqlclient.impl.codec.GenericPacketPayloadDecoder;
import io.reactiverse.mysqlclient.impl.codec.MySQLCommandBase;
import io.reactiverse.mysqlclient.impl.codec.MySQLCommandResponse;
import io.reactiverse.mysqlclient.impl.codec.MySQLPacketDecoder;
import io.reactiverse.mysqlclient.impl.codec.datatype.DataFormat;
import io.reactiverse.mysqlclient.impl.codec.datatype.DataType;
import io.reactiverse.mysqlclient.impl.codec.encoder.PreparedStatementExecuteCommand;
import io.reactiverse.mysqlclient.impl.codec.encoder.PreparedStatementPrepareCommand;
import io.reactiverse.mysqlclient.impl.codec.encoder.QueryCommand;
import io.reactiverse.mysqlclient.impl.protocol.CommandType;
import io.reactiverse.mysqlclient.impl.protocol.backend.ColumnDefinition;
import io.reactiverse.mysqlclient.impl.util.BufferUtils;

import java.nio.charset.Charset;
import java.util.List;

import static io.reactiverse.mysqlclient.impl.protocol.backend.EofPacket.EOF_PACKET_HEADER;
import static io.reactiverse.mysqlclient.impl.protocol.backend.ErrPacket.ERROR_PACKET_HEADER;
import static io.reactiverse.mysqlclient.impl.protocol.backend.OkPacket.OK_PACKET_HEADER;

public class MySQLCommandHandler extends MySQLPacketDecoder {
  private CommandHandlerState commandHandlerState;

  // Resultset intermediary result
  private int columnCount;
  private int processingIndex;
  private ColumnDefinition[] columnDefinitions;

  // prepare intermediary result
  private int paramCount;
//  private int processingParamIndex;
  private MySQLPreparedStatement preparedStatement;

  public MySQLCommandHandler(Charset charset, MySQLSocketConnection socketConnection) {
    super(charset, socketConnection);
    resetIntermediaryResult();
  }

  @Override
  protected void decodePayload(ChannelHandlerContext ctx, ByteBuf payload, int payloadLength, int sequenceId, List<Object> out) {
    MySQLCommandBase<?> executingCmd = socketConnection.getExecutingCmd().getCmd();
    if (executingCmd == null) {
      ctx.fireChannelRead(MySQLCommandResponse.failure("Unknown command received"));
      return;
    }
    switch (executingCmd.getCommandType()) {
      case CommandType.COM_PING:
        handleOkPacketPayload(ctx, payload);
        break;
      case CommandType.COM_QUERY:
        QueryCommand queryCommand = (QueryCommand) executingCmd;
        switch (commandHandlerState) {
          case INIT:
            // may receive ERR_Packet, OK_Packet, LOCAL INFILE Request, Text Resultset
            int firstByte = payload.getUnsignedByte(payload.readerIndex());
            if (firstByte == OK_PACKET_HEADER) {
              payload.readByte();
              queryCommand.handleEndPacket(GenericPacketPayloadDecoder.decodeOkPacketBody(payload, charset));
              handleResultsetDecodingCompleted(ctx, queryCommand);
            } else if (firstByte == ERROR_PACKET_HEADER) {
              handleErrorPacketPayload(ctx, payload);
            } else if (firstByte == 0xFB) {
              //TODO LOCAL INFILE Request support
            } else {
              //regarded as Resultset handling
              decodeColumnCountPacketPayload(payload);
              commandHandlerState = CommandHandlerState.HANDLING_COLUMN_DEFINITION;
              this.columnDefinitions = new ColumnDefinition[this.columnCount];
            }
            break;
          case HANDLING_COLUMN_DEFINITION:
            decodeColumnDefinitionPacketPayload(payload);
            if (processingIndex == columnCount) {
              // all column definitions have been handled, switch to row data handling
              commandHandlerState = CommandHandlerState.HANDLING_ROW_DATA_OR_END_PACKET;
              queryCommand.handleColumnMetadata(new ColumnMetadata(columnDefinitions, DataFormat.TEXT));
            }
            break;
          case HANDLING_ROW_DATA_OR_END_PACKET:
            /*
              Resultset row can begin with 0xfe byte (when using text protocol with a field length > 0xffffff)
              To ensure that packets beginning with 0xfe correspond to the ending packet (EOF_Packet or OK_Packet with a 0xFE header),
              the packet length must be checked and must be less than 0xffffff in length.
             */
            int first = payload.getUnsignedByte(payload.readerIndex());
            if (first == ERROR_PACKET_HEADER) {
              handleErrorPacketPayload(ctx, payload);
              resetIntermediaryResult();
            }
            // enabling CLIENT_DEPRECATE_EOF capability will receive an OK_Packet with a EOF_Packet header here
            // we need check this is not a row data by checking packet length < 0xFFFFFF
            else if (first == EOF_PACKET_HEADER && payloadLength < 0xFFFFFF) {
              payload.readByte();
              queryCommand.handleEndPacket(GenericPacketPayloadDecoder.decodeOkPacketBody(payload, charset));
              handleResultsetDecodingCompleted(ctx, queryCommand);
              resetIntermediaryResult();
            } else {
              // accept a row data
              queryCommand.decoder.decodeRow(columnCount, payload);
            }
            break;
        }
        break;
      case CommandType.COM_STMT_PREPARE:
        PreparedStatementPrepareCommand preparedStatementPrepareCommand = (PreparedStatementPrepareCommand) executingCmd;
        switch (commandHandlerState) {
          case INIT:
            int firstByte = payload.getUnsignedByte(payload.readerIndex());
            if (firstByte == ERROR_PACKET_HEADER) {
              handleErrorPacketPayload(ctx, payload);
            } else {
              // handle COM_STMT_PREPARE response
              payload.readUnsignedByte(); // 0x00: OK
              long statementId = payload.readUnsignedIntLE();
              int numberOfColumns = payload.readUnsignedShortLE();
              int numberOfParameters = payload.readUnsignedShortLE();
              payload.readByte(); // [00] filler
              int numberOfWarnings = payload.readShortLE();

              // handle metadata here
              this.columnCount = numberOfColumns;
              this.paramCount = numberOfParameters;
              preparedStatement = new MySQLPreparedStatement();
              preparedStatement.statementId = statementId;

              this.columnDefinitions = new ColumnDefinition[paramCount];
              this.commandHandlerState = CommandHandlerState.HANDLING_PARAM_COLUMN_DEFINITION;
            }
            break;
          case HANDLING_PARAM_COLUMN_DEFINITION:
            decodeColumnDefinitionPacketPayload(payload);
            if (processingIndex == paramCount) {
              preparedStatement.paramsColumnDefinitions = this.columnDefinitions;
              this.processingIndex = 0;
              this.columnDefinitions = new ColumnDefinition[columnCount];
              this.commandHandlerState = CommandHandlerState.HANDLING_COLUMN_COLUMN_DEFINITION;
            }
            break;
          case HANDLING_COLUMN_COLUMN_DEFINITION:
            decodeColumnDefinitionPacketPayload(payload);
            if (processingIndex == columnCount) {
              preparedStatement.columnMetadata = new ColumnMetadata(columnDefinitions, DataFormat.BINARY);
              ctx.fireChannelRead(MySQLCommandResponse.success(preparedStatement));
              resetIntermediaryResult();
            }
            break;
        }
        break;
      case CommandType.COM_STMT_EXECUTE:
        //FIXME
        // can be reused with COM_QUERY Response handling
        // we may abstract a base codec for simple_query/prepared_query response handling later
        PreparedStatementExecuteCommand preparedStatementExecuteCommand = (PreparedStatementExecuteCommand) executingCmd;
        switch (commandHandlerState) {
          case INIT:
            int firstByte = payload.getUnsignedByte(payload.readerIndex());
            if (firstByte == ERROR_PACKET_HEADER) {
              handleErrorPacketPayload(ctx, payload);
            } else if (firstByte == OK_PACKET_HEADER) {
              handleOkPacketPayload(ctx, payload);
            } else {
              //regarded as Resultset handling
              decodeColumnCountPacketPayload(payload);
              commandHandlerState = CommandHandlerState.HANDLING_COLUMN_DEFINITION;
              this.columnDefinitions = new ColumnDefinition[this.columnCount];
            }
            break;
          case HANDLING_COLUMN_DEFINITION:
            decodeColumnDefinitionPacketPayload(payload);
            if (processingIndex == columnCount) {
              // all column definitions have been handled, switch to row data handling
              commandHandlerState = CommandHandlerState.HANDLING_ROW_DATA_OR_END_PACKET;
              preparedStatementExecuteCommand.handleColumnMetadata(new ColumnMetadata(columnDefinitions, DataFormat.BINARY));
            }
            break;
          case HANDLING_ROW_DATA_OR_END_PACKET:
            /*
              Resultset row can begin with 0xfe byte (when using text protocol with a field length > 0xffffff)
              To ensure that packets beginning with 0xfe correspond to the ending packet (EOF_Packet or OK_Packet with a 0xFE header),
              the packet length must be checked and must be less than 0xffffff in length.
             */
            int first = payload.getUnsignedByte(payload.readerIndex());
            if (first == ERROR_PACKET_HEADER) {
              handleErrorPacketPayload(ctx, payload);
              resetIntermediaryResult();
            }
            // enabling CLIENT_DEPRECATE_EOF capability will receive an OK_Packet with a EOF_Packet header here
            // we need check this is not a row data by checking packet length < 0xFFFFFF
            else if (first == EOF_PACKET_HEADER && payloadLength < 0xFFFFFF) {
              payload.readByte();
              preparedStatementExecuteCommand.handleEndPacket(GenericPacketPayloadDecoder.decodeOkPacketBody(payload, charset));
              handleResultsetDecodingCompleted(ctx, preparedStatementExecuteCommand);
              resetIntermediaryResult();
            } else {
              // accept a row data
              preparedStatementExecuteCommand.decoder.decodeRow(columnCount, payload);
            }
            break;
        }
        break;
    }
  }

  private void handleOkPacketPayload(ChannelHandlerContext ctx, ByteBuf payload) {
    // header should be OK_PACKET_HEADER
    payload.readUnsignedByte();
    ctx.fireChannelRead(MySQLCommandResponse.success(null));
  }

  private void handleErrorPacketPayload(ChannelHandlerContext ctx, ByteBuf payload) {
    // header should be ERROR_PACKET_HEADER
    payload.readUnsignedByte();
    ctx.fireChannelRead(MySQLCommandResponse.failure(MySQLExceptionFactory.throwNewException(GenericPacketPayloadDecoder.decodeErrPacketBody(payload, charset))));
  }


  private void decodeColumnCountPacketPayload(ByteBuf payload) {
    long columnCount = BufferUtils.readLengthEncodedInteger(payload);
    this.columnCount = (int) columnCount;
  }

  private void decodeColumnDefinitionPacketPayload(ByteBuf payload) {
    String catalog = BufferUtils.readLengthEncodedString(payload, charset);
    String schema = BufferUtils.readLengthEncodedString(payload, charset);
    String table = BufferUtils.readLengthEncodedString(payload, charset);
    String orgTable = BufferUtils.readLengthEncodedString(payload, charset);
    String name = BufferUtils.readLengthEncodedString(payload, charset);
    String orgName = BufferUtils.readLengthEncodedString(payload, charset);
    long lengthOfFixedLengthFields = BufferUtils.readLengthEncodedInteger(payload);
    int characterSet = payload.readUnsignedShortLE();
    long columnLength = payload.readUnsignedIntLE();
    DataType type = DataType.valueOf(payload.readUnsignedByte());
    int flags = payload.readUnsignedShortLE();
    byte decimals = payload.readByte();

    ColumnDefinition columnDefinition = new ColumnDefinition(catalog, schema, table, orgTable, name, orgName, characterSet, columnLength, type, flags, decimals);
    columnDefinitions[processingIndex] = columnDefinition;
    processingIndex++;
  }

  private void handleResultsetDecodingCompleted(ChannelHandlerContext ctx, MySQLCommandBase cmd) {
    MySQLCommandResponse<?> response;
    if (cmd.getFailure() != null) {
      response = MySQLCommandResponse.failure(cmd.getFailure());
    } else {
      response = MySQLCommandResponse.success(cmd.getResult());
    }
    ctx.fireChannelRead(response);
  }

  private void resetIntermediaryResult() {
    this.commandHandlerState = CommandHandlerState.INIT;
    this.columnCount = 0;
    this.processingIndex = 0;
    this.columnDefinitions = null;
    this.paramCount = 0;
    this.preparedStatement = null;
  }

  private enum CommandHandlerState {
    INIT, // BASE
    HANDLING_COLUMN_DEFINITION, HANDLING_ROW_DATA_OR_END_PACKET, // for COM_QUERY, COM_EXECUTING
    HANDLING_PARAM_COLUMN_DEFINITION, HANDLING_COLUMN_COLUMN_DEFINITION // for COM_PREPARE
  }
}

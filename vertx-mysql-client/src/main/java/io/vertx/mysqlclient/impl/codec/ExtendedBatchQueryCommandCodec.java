package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.datatype.DataType;
import io.vertx.mysqlclient.impl.datatype.DataTypeCodec;
import io.vertx.mysqlclient.impl.protocol.CommandType;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;

import java.util.List;

import static io.vertx.mysqlclient.impl.protocol.Packets.EnumCursorType.CURSOR_TYPE_NO_CURSOR;

class ExtendedBatchQueryCommandCodec<R> extends ExtendedQueryCommandBaseCodec<R, ExtendedQueryCommand<R>> {

  private List<Tuple> params;
  private int batchIdx = 0;

  ExtendedBatchQueryCommandCodec(ExtendedQueryCommand<R> cmd) {
    super(cmd);
    params = cmd.paramsList();
  }

  @Override
  void encode(MySQLEncoder encoder) {
    super.encode(encoder);
    if (params.isEmpty() && statement.paramDesc.paramDefinitions().length > 0) {
      completionHandler.handle(CommandResponse.failure("Statement parameter is not set because of the empty batch param list"));
      return;
    }
    doExecuteBatch();
  }

  @Override
  protected void handleSingleResultsetDecodingCompleted(int serverStatusFlags, long affectedRows, long lastInsertId) {
    super.handleSingleResultsetDecodingCompleted(serverStatusFlags, affectedRows, lastInsertId);
    doExecuteBatch();
  }

  @Override
  protected boolean isDecodingCompleted(int serverStatusFlags) {
    return super.isDecodingCompleted(serverStatusFlags) && batchIdx == params.size();
  }

  private void doExecuteBatch() {
    if (batchIdx < params.size()) {
      sequenceId = 0;
      Tuple param = params.get(batchIdx);
      // binding parameters
      String bindMsg = statement.bindParameters(param);
      if (bindMsg != null) {
        completionHandler.handle(CommandResponse.failure(bindMsg));
      }
      sendBatchStatementExecuteCommand(statement, param);
      batchIdx++;
    }
  }

  private void sendBatchStatementExecuteCommand(MySQLPreparedStatement statement, Tuple params) {
    ByteBuf packet = allocateBuffer();
    // encode packet header
    int packetStartIdx = packet.writerIndex();
    packet.writeMediumLE(0); // will set payload length later by calculation
    packet.writeByte(sequenceId);

    // encode packet payload
    packet.writeByte(CommandType.COM_STMT_EXECUTE);
    packet.writeIntLE((int) statement.statementId);
    packet.writeByte(CURSOR_TYPE_NO_CURSOR);
    // iteration count, always 1
    packet.writeIntLE(1);

    /*
     * Null-bit map and type should always be reconstructed for every batch of parameters here
     */
    int numOfParams = statement.bindingTypes().length;
    int bitmapLength = (numOfParams + 7) / 8;
    byte[] nullBitmap = new byte[bitmapLength];

    int pos = packet.writerIndex();

    if (numOfParams > 0) {
      // write a dummy bitmap first
      packet.writeBytes(nullBitmap);
      packet.writeByte(1);
      for (int i = 0; i < params.size(); i++) {
        Object param = params.getValue(i);
        DataType dataType = DataTypeCodec.inferDataTypeByEncodingValue(param);
        packet.writeByte(dataType.id);
        packet.writeByte(0); // parameter flag: signed
      }

      for (int i = 0; i < numOfParams; i++) {
        Object value = params.getValue(i);
        if (value != null) {
          DataTypeCodec.encodeBinary(DataTypeCodec.inferDataTypeByEncodingValue(value), value, encoder.encodingCharset, packet);
        } else {
          nullBitmap[i / 8] |= (1 << (i & 7));
        }
      }

      // padding null-bitmap content
      packet.setBytes(pos, nullBitmap);
    }

    // set payload length
    int payloadLength = packet.writerIndex() - packetStartIdx - 4;
    packet.setMediumLE(packetStartIdx, payloadLength);

    sendPacket(packet, payloadLength);
  }
}

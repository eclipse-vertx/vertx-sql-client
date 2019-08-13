package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.ExtendedBatchQueryCommand;

import java.util.List;

import static io.vertx.mysqlclient.impl.codec.Packets.*;

class ExtendedBatchQueryCommandCodec<R> extends ExtendedQueryCommandBaseCodec<R, ExtendedBatchQueryCommand<R>> {

  private List<Tuple> params;
  private int batchIdx = 0;

  ExtendedBatchQueryCommandCodec(ExtendedBatchQueryCommand<R> cmd) {
    super(cmd);
    params = cmd.params();
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
  protected void handleSingleResultsetDecodingCompleted(int serverStatusFlags, int affectedRows, int lastInsertId) {
    super.handleSingleResultsetDecodingCompleted(serverStatusFlags, affectedRows, lastInsertId);
    doExecuteBatch();
  }

  @Override
  protected boolean isDecodingCompleted(int serverStatusFlags) {
    return super.isDecodingCompleted(serverStatusFlags) && batchIdx == params.size();
  }

  private void doExecuteBatch() {
    if (batchIdx < params.size()) {
      this.sequenceId = 0;
      Tuple param = params.get(batchIdx);
      sendStatementExecuteCommand(statement.statementId, statement.paramDesc.paramDefinitions(), sendType, param, (byte) 0x00);
      batchIdx++;
    }
  }
}

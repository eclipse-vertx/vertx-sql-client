package io.vertx.pgclient.impl.codec;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.BufferImpl;
import io.vertx.sqlclient.SqlResult;

class CopyOutCommandCodec extends PgCommandCodec<SqlResult<Buffer>, CopyOutCommand> {
  CopyOutDataDecoder decoder;

  CopyOutCommandCodec(CopyOutCommand cmd) {
    super(cmd);
    decoder = new CopyOutDataDecoder(cmd.collector());
  }

  @Override
  public void handleCommandComplete(int updated) {
    this.result = null;
    Buffer result;
    Throwable failure;
    int size;
    if (decoder != null) {
      failure = decoder.complete();
      result = decoder.result();
      size = decoder.size();
      decoder.reset();
    } else {
      failure = null;
      result = new BufferImpl();
      size = 0;
    }
    cmd.resultHandler().handleResult(updated, size, null, result, failure);
  }

  @Override
  public void handleErrorResponse(ErrorResponse errorResponse) {
    failure = errorResponse.toException();
  }

  void encode(PgEncoder encoder) {
    encoder.writeQuery(new Query(cmd.sql()));
  }
}

package io.vertx.pgclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.impl.QueryResultBuilder;
import io.vertx.sqlclient.impl.SqlResultImpl;
import io.vertx.sqlclient.impl.command.CommandBase;

import java.util.stream.Collector;

public class CopyOutCommand extends CommandBase<Boolean> {
  private final String sql;
  private final Collector<ByteBuf, Buffer, Buffer> collector;
  private final QueryResultBuilder<Buffer, SqlResultImpl<Buffer>, SqlResult<Buffer>> resultHandler;

  public CopyOutCommand(
    String sql,
    QueryResultBuilder<Buffer, SqlResultImpl<Buffer>, SqlResult<Buffer>> resultHandler
  ) {
    this.sql = sql;
    this.resultHandler = resultHandler;
    this.collector = Collector.of(
      Buffer::buffer,
      (v, chunk) -> v.appendBuffer(Buffer.buffer(chunk)),
      (v1, v2) -> null,
      (finalResult) -> finalResult
    );
  }

  QueryResultBuilder<Buffer, SqlResultImpl<Buffer>, SqlResult<Buffer>> resultHandler() {
    return resultHandler;
  }

  String sql() {
    return sql;
  }

  Collector<ByteBuf, Buffer, Buffer> collector() {
    return collector;
  }
}

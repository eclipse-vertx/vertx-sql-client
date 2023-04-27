package io.vertx.pgclient.impl.codec;

import io.vertx.core.buffer.Buffer;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.impl.QueryResultBuilder;
import io.vertx.sqlclient.impl.SqlResultImpl;
import io.vertx.sqlclient.impl.command.CommandBase;

public class CopyOutCommand extends CommandBase<Buffer> {
  private final String sql;
  private final QueryResultBuilder<Buffer, SqlResultImpl<Buffer>, SqlResult<Buffer>> resultHandler;

  public CopyOutCommand(String sql, QueryResultBuilder<Buffer, SqlResultImpl<Buffer>, SqlResult<Buffer>> resultHandler) {
    this.sql = sql;
    this.resultHandler = resultHandler;
  }
}

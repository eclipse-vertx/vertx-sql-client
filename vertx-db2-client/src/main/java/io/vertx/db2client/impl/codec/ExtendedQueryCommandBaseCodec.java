package io.vertx.db2client.impl.codec;

import io.vertx.sqlclient.impl.command.ExtendedQueryCommandBase;

abstract class ExtendedQueryCommandBaseCodec<R, C extends ExtendedQueryCommandBase<R>> extends QueryCommandBaseCodec<R, C> {

  protected final DB2PreparedStatement statement;

  ExtendedQueryCommandBaseCodec(C cmd) {
    super(cmd);
    statement = (DB2PreparedStatement) cmd.preparedStatement();
  }

}

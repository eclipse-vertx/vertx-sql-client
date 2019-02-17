package io.reactiverse.mysqlclient.impl.codec.encoder;

import io.reactiverse.mysqlclient.impl.MySQLPreparedStatement;
import io.reactiverse.mysqlclient.impl.QueryResultHandler;
import io.reactiverse.mysqlclient.impl.RowResultDecoder;
import io.reactiverse.mysqlclient.impl.codec.MySQLCommandBase;
import io.reactiverse.mysqlclient.impl.codec.MySQLPacketEncoder;
import io.reactiverse.mysqlclient.impl.protocol.CommandType;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;

import java.util.stream.Collector;

public class PreparedStatementExecuteCommand<R> extends MySQLCommandBase<Boolean> {
  private MySQLPreparedStatement mySQLPreparedStatement;
  private Tuple params;

  // TODO handle re-bound situations?
  // Flag if parameters must be re-bound
  private final byte sendType = 1;

  // QueryCommandBase
  public RowResultDecoder<?, R> decoder;
  final QueryResultHandler<R> resultHandler;
  final Collector<Row, ?, R> collector;

  public PreparedStatementExecuteCommand(MySQLPreparedStatement mySQLPreparedStatement,
                                         Tuple params,
                                         boolean singleton,
                                         Collector<Row, ?, R> collector,
                                         QueryResultHandler<R> resultHandler) {
    super(CommandType.COM_STMT_EXECUTE);
    this.mySQLPreparedStatement = mySQLPreparedStatement;
    this.params = params;

    this.resultHandler = resultHandler;
    this.collector = collector;
    this.decoder = new RowResultDecoder<>(collector, singleton, mySQLPreparedStatement.columnMetadata);
  }

  @Override
  public void exec(MySQLPacketEncoder out) {
    out.writeExecuteMessage(mySQLPreparedStatement.statementId, mySQLPreparedStatement.paramsColumnDefinitions, sendType, params);
  }
}

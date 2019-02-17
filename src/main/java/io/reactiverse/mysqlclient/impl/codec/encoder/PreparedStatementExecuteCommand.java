package io.reactiverse.mysqlclient.impl.codec.encoder;

import io.reactiverse.mysqlclient.impl.ColumnMetadata;
import io.reactiverse.mysqlclient.impl.MySQLPreparedStatement;
import io.reactiverse.mysqlclient.impl.QueryResultHandler;
import io.reactiverse.mysqlclient.impl.RowResultDecoder;
import io.reactiverse.mysqlclient.impl.codec.MySQLCommandBase;
import io.reactiverse.mysqlclient.impl.codec.MySQLPacketEncoder;
import io.reactiverse.mysqlclient.impl.protocol.CommandType;
import io.reactiverse.mysqlclient.impl.protocol.backend.OkPacket;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;

import java.util.stream.Collector;

public class PreparedStatementExecuteCommand<R> extends MySQLCommandBase<Boolean> {
  private MySQLPreparedStatement mySQLPreparedStatement;
  private Tuple params;

  // TODO handle re-bound situations?
  // Flag if parameters must be re-bound
  private final byte sendType = 1;

  private final boolean singleton;
  //FIXME QueryCommandBase
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
    this.singleton = singleton;
    this.decoder = new RowResultDecoder<>(collector, singleton, mySQLPreparedStatement.columnMetadata);
  }

  @Override
  public void exec(MySQLPacketEncoder out) {
    out.writeExecuteMessage(mySQLPreparedStatement.statementId, mySQLPreparedStatement.paramsColumnDefinitions, sendType, params);
  }

  public void handleColumnMetadata(ColumnMetadata columnMetadata) {
    decoder = new RowResultDecoder<>(collector, singleton, columnMetadata);
  }

  //TODO MySQL provides last_insert_id, warnings, server session track, does our API need this?
  public void handleEndPacket(OkPacket okPacket) {
    this.setResult(false);
    R result;
    int size;
    ColumnMetadata columnMetadata;
    if (decoder != null) {
      result = decoder.complete();
      columnMetadata = decoder.columnMetadata();
      size = decoder.size();
      decoder.reset();
    } else {
      result = emptyResult(collector);
      size = 0;
      columnMetadata = null;
    }
    resultHandler.handleResult((int) okPacket.getAffectedRows(), size, columnMetadata, result);
  }

  private static <A, T> T emptyResult(Collector<Row, A, T> collector) {
    return collector.finisher().apply(collector.supplier().get());
  }
}

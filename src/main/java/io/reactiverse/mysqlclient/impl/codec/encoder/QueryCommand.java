package io.reactiverse.mysqlclient.impl.codec.encoder;

import io.reactiverse.mysqlclient.impl.ColumnMetadata;
import io.reactiverse.mysqlclient.impl.QueryResultHandler;
import io.reactiverse.mysqlclient.impl.RowResultDecoder;
import io.reactiverse.mysqlclient.impl.codec.MySQLCommandBase;
import io.reactiverse.mysqlclient.impl.codec.MySQLPacketEncoder;
import io.reactiverse.mysqlclient.impl.protocol.CommandType;
import io.reactiverse.mysqlclient.impl.protocol.backend.OkPacket;
import io.reactiverse.pgclient.Row;

import java.util.stream.Collector;

public class QueryCommand<T> extends MySQLCommandBase<Boolean> {
  //FIXME QueryCommandBase
  final QueryResultHandler<T> resultHandler;
  final Collector<Row, ?, T> collector;
  private final String sql;
  private final boolean singleton;
  public RowResultDecoder<?, T> decoder;


  public QueryCommand(String sql,
                      boolean singleton,
                      Collector<Row, ?, T> collector,
                      QueryResultHandler<T> resultHandler) {
    super(CommandType.COM_QUERY);
    this.singleton = singleton;
    this.sql = sql;
    this.collector = collector;
    this.resultHandler = resultHandler;
  }

  private static <A, T> T emptyResult(Collector<Row, A, T> collector) {
    return collector.finisher().apply(collector.supplier().get());
  }

  @Override
  public void exec(MySQLPacketEncoder out) {
    out.writeQueryMessage(sql);
  }

  public void handleColumnMetadata(ColumnMetadata columnMetadata) {
    decoder = new RowResultDecoder<>(collector, singleton, columnMetadata);
  }

  //TODO MySQL provides last_insert_id, warnings, server session track, does our API need this?
  public void handleEndPacket(OkPacket okPacket) {
    this.setResult(false);
    T result;
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
}

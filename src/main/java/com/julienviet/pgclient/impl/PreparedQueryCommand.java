package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.decoder.message.BindComplete;
import com.julienviet.pgclient.codec.decoder.message.NoData;
import com.julienviet.pgclient.codec.decoder.message.ParameterDescription;
import com.julienviet.pgclient.codec.decoder.message.ParseComplete;
import com.julienviet.pgclient.codec.decoder.message.ReadyForQuery;
import com.julienviet.pgclient.codec.encoder.message.Bind;
import com.julienviet.pgclient.codec.encoder.message.Describe;
import com.julienviet.pgclient.codec.encoder.message.Execute;
import com.julienviet.pgclient.codec.encoder.message.Parse;
import com.julienviet.pgclient.codec.encoder.message.Sync;
import com.julienviet.pgclient.codec.util.Util;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class PreparedQueryCommand extends QueryCommandBase {


  final PreparedStatementImpl ps;
  final List<Object> params;
  final Handler<AsyncResult<ResultSet>> handler;
  private ResultSet result;

  PreparedQueryCommand(PreparedStatementImpl ps, List<Object> params, Handler<AsyncResult<ResultSet>> handler) {
    this.ps = ps;
    this.params = params;
    this.handler = handler;
  }

  @Override
  void handleDescription(List<String> columnNames) {
    result = new ResultSet().setColumnNames(columnNames).setResults(new ArrayList<>());
  }

  @Override
  void handleRow(JsonArray row) {
    result.getResults().add(row);
  }

  @Override
  boolean exec(DbConnection conn) {
    if (!ps.parsed) {
      ps.parsed = true;
      conn.writeToChannel(new Parse(ps.sql).setStatement(ps.stmt));
    }
    conn.writeToChannel(new Bind().setParamValues(Util.paramValues(params)).setStatement(ps.stmt));
    conn.writeToChannel(new Describe().setStatement(ps.stmt));
    conn.writeToChannel(new Execute().setRowCount(0));
    conn.writeToChannel(Sync.INSTANCE);
    return true;
  }

  @Override
  public boolean handleMessage(Message msg) {
    if (msg.getClass() == ReadyForQuery.class) {
      handler.handle(Future.succeededFuture(result));
      return true;
    } else if (msg.getClass() == ParameterDescription.class) {
      return false;
    } else if (msg.getClass() == NoData.class) {
      return false;
    } else if (msg.getClass() == ParseComplete.class) {
      return false;
    } else if (msg.getClass() == BindComplete.class) {
      return false;
    } else {
      return super.handleMessage(msg);
    }
  }

  @Override
  void handleComplete() {
  }

  @Override
  void fail(Throwable cause) {
    throw new UnsupportedOperationException("Not yet implemented");
  }
}

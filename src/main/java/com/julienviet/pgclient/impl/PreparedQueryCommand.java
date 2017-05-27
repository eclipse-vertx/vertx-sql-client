package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.PgResultSet;
import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.decoder.message.BindComplete;
import com.julienviet.pgclient.codec.decoder.message.NoData;
import com.julienviet.pgclient.codec.decoder.message.ParameterDescription;
import com.julienviet.pgclient.codec.decoder.message.ParseComplete;
import com.julienviet.pgclient.codec.decoder.message.PortalSuspended;
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

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class PreparedQueryCommand extends QueryCommandBase {


  final PreparedStatementImpl ps;
  final List<Object> params;
  final int fetch;
  final Handler<AsyncResult<PgResultSet>> handler;
  private PgResultSet result;
  private final String portal;
  private final boolean suspended;
  private Throwable failure;

  PreparedQueryCommand(PreparedStatementImpl ps, List<Object> params, Handler<AsyncResult<PgResultSet>> handler) {
    this(ps, params, 0, "", false, handler);
  }
  PreparedQueryCommand(PreparedStatementImpl ps, List<Object> params, int fetch, String portal, boolean suspended, Handler<AsyncResult<PgResultSet>> handler) {
    this.ps = ps;
    this.params = params;
    this.handler = handler;
    this.fetch = fetch;
    this.portal = portal;
    this.suspended = suspended;
  }

  @Override
  void handleDescription(List<String> columnNames) {
    result = new PgResultSet().setComplete(true).setColumnNames(columnNames).setResults(new ArrayList<>());
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
    if (!suspended) {
      conn.writeToChannel(new Bind().setParamValues(Util.paramValues(params)).setPortal(portal).setStatement(ps.stmt));
      conn.writeToChannel(new Describe().setStatement(ps.stmt));
    } else {
      // Needed for now, later see how to remove it
      conn.writeToChannel(new Describe().setPortal(portal));
    }
    conn.writeToChannel(new Execute().setPortal(portal).setRowCount(fetch));
    conn.writeToChannel(Sync.INSTANCE);
    return true;
  }

  @Override
  public boolean handleMessage(Message msg) {
    if (msg.getClass() == ReadyForQuery.class) {
      if (failure != null) {
        handler.handle(Future.failedFuture(failure));
      } else {
        handler.handle(Future.succeededFuture(result));
      }
      return true;
    } else if (msg.getClass() == PortalSuspended.class) {
      result.setComplete(false);
      return false;
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
    failure = cause;
  }
}

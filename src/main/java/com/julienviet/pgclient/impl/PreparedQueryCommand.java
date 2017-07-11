package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.decoder.message.BindComplete;
import com.julienviet.pgclient.codec.decoder.message.NoData;
import com.julienviet.pgclient.codec.decoder.message.ParameterDescription;
import com.julienviet.pgclient.codec.decoder.message.ParseComplete;
import com.julienviet.pgclient.codec.decoder.message.PortalSuspended;
import com.julienviet.pgclient.codec.encoder.message.Bind;
import com.julienviet.pgclient.codec.encoder.message.Describe;
import com.julienviet.pgclient.codec.encoder.message.Execute;
import com.julienviet.pgclient.codec.encoder.message.Parse;
import com.julienviet.pgclient.codec.encoder.message.Sync;
import com.julienviet.pgclient.codec.util.Util;

import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class PreparedQueryCommand extends QueryCommandBase {


  final boolean parse;
  final String sql;
  final List<Object> params;
  final int fetch;
  final String stmt;
  private final String portal;
  private final boolean suspended;

  PreparedQueryCommand(String sql,
                       List<Object> params,
                       QueryResultHandler handler) {
    this(true, sql, params, 0, "", "", false, handler);
  }
  PreparedQueryCommand(boolean parse,
                       String sql,
                       List<Object> params,
                       int fetch,
                       String stmt,
                       String portal,
                       boolean suspended,
                       QueryResultHandler handler) {
    super(handler);
    this.parse = parse;
    this.sql = sql;
    this.params = params;
    this.fetch = fetch;
    this.stmt = stmt;
    this.portal = portal;
    this.suspended = suspended;
  }

  @Override
  void exec(DbConnection conn) {
    if (parse) {
      conn.writeMessage(new Parse(sql).setStatement(stmt));
    }
    if (!suspended) {
      conn.writeMessage(new Bind().setParamValues(Util.paramValues(params)).setPortal(portal).setStatement(stmt));
      conn.writeMessage(new Describe().setStatement(stmt));
    } else {
      // Needed for now, later see how to remove it
      conn.writeMessage(new Describe().setPortal(portal));
    }
    conn.writeMessage(new Execute().setPortal(portal).setRowCount(fetch));
    conn.writeMessage(Sync.INSTANCE);
  }

  @Override
  public boolean handleMessage(Message msg) {
    if (msg.getClass() == PortalSuspended.class) {
      handler.endResult(true);
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
}

package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.Result;

import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class BatchExecuteCommand implements Command {


  final String sql;
  final boolean sync;
  final String stmt;
  final List<Object> params;

  public BatchExecuteCommand(String sql, boolean sync, String stmt, List<Object> params) {
    this.sql = sql;
    this.sync = sync;
    this.stmt = stmt;
    this.params = params;
  }
}

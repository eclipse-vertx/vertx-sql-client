package com.julienviet.pgclient.impl;

import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class BatchExecuteCommand implements Command {


  final PreparedStatementImpl ps;
  final boolean sync;
  final List<Object> params;

  public BatchExecuteCommand(PreparedStatementImpl ps, boolean sync, List<Object> params) {
    this.ps = ps;
    this.sync = sync;
    this.params = params;
  }
}

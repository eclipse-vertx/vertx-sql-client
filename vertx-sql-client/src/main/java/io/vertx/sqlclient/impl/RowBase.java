package io.vertx.sqlclient.impl;

import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.internal.ArrayTuple;
import io.vertx.sqlclient.internal.RowInternal;

import java.util.Collection;

/**
 * Base class for rows.
 */
public abstract class RowBase extends ArrayTuple implements RowInternal {

  private boolean released;

  public RowBase(int len) {
    super(len);
  }

  public RowBase(Collection<?> c) {
    super(c);
  }

  public RowBase(Tuple tuple) {
    super(tuple);
  }

  @Override
  public void release() {
    released = true;
  }

  @Override
  public boolean tryRecycle() {
    boolean ret = released;
    if (ret) {
      clear();
    }
    return ret;
  }
}

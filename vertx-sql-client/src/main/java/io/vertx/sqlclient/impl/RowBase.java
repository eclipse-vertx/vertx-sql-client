package io.vertx.sqlclient.impl;

import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.internal.ArrayTuple;
import io.vertx.sqlclient.internal.RowDescriptor;
import io.vertx.sqlclient.internal.RowInternal;

import java.util.Collection;
import java.util.List;

/**
 * Base class for rows.
 */
public class RowBase extends ArrayTuple implements RowInternal {

  private boolean released;
  protected final RowDescriptor desc;

  public RowBase(RowDescriptor desc) {
    super(desc.columnNames().size());

    this.desc = desc;
  }

  public RowBase(RowDescriptor desc, Collection<?> c) {
    super(c);

    this.desc = desc;
  }

  public RowBase(RowDescriptor desc, Tuple tuple) {
    super(tuple);

    this.desc = desc;
  }

  @Override
  public String getColumnName(int pos) {
    List<String> columnNames = desc.columnNames();
    return pos < 0 || columnNames.size() - 1 < pos ? null : columnNames.get(pos);
  }

  @Override
  public int getColumnIndex(String name) {
    if (name == null) {
      throw new NullPointerException();
    }
    return desc.columnIndex(name);
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

package io.vertx.sqlclient.impl;

import io.vertx.sqlclient.Tuple;

public interface TupleInternal extends Tuple {

  void setValue(int pos, Object value);

}

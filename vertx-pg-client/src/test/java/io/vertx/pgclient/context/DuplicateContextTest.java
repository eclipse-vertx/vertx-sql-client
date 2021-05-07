package io.vertx.pgclient.context;


import io.vertx.core.Context;
import io.vertx.core.impl.ContextInternal;

public class DuplicateContextTest extends ContextTest {

  @Override
  protected Context createContext() {
    return ((ContextInternal)vertx.getOrCreateContext()).duplicate();
  }
}

package io.vertx.tests.pgclient.context;


import io.vertx.core.Context;
import io.vertx.core.internal.ContextInternal;

public class DuplicateContextTest extends ContextTest {

  @Override
  protected Context createContext() {
    return ((ContextInternal)vertx.getOrCreateContext()).duplicate();
  }
}

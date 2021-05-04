package io.vertx.pgclient.context;

import io.vertx.core.Context;

public class EventLoopContextTest extends ContextTest {

  @Override
  protected Context createContext() {
    return vertx.getOrCreateContext();
  }
}

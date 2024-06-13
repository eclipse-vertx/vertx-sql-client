package io.vertx.pgclient.context;


import io.vertx.core.Context;
import io.vertx.core.internal.VertxInternal;

public class WorkerContextTest extends ContextTest {

  @Override
  protected Context createContext() {
    return ((VertxInternal)vertx).createWorkerContext();
  }
}

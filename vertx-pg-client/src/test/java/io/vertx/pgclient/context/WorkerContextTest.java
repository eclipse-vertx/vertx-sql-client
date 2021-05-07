package io.vertx.pgclient.context;


import io.vertx.core.Context;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;

public class WorkerContextTest extends ContextTest {

  @Override
  protected Context createContext() {
    return ((VertxInternal)vertx).createWorkerContext();
  }
}

package io.reactiverse.pgclient.impl;

import io.reactiverse.pgclient.PgPool;
import io.reactiverse.pgclient.PgPoolOptions;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.Shareable;

/**
 * @author <a href="https://github.com/mystdeim">Roman Novikov</a>
 */
class PgPoolHolder implements Shareable {

  private final Vertx vertx;
  private final PgPoolOptions options;
  private final Runnable closeRunner;

  private PgPool client;
  private int refCount = 1;

  PgPoolHolder(Vertx vertx, PgPoolOptions options, Runnable closeRunner) {
    this.vertx = vertx;
    this.options = options;
    this.closeRunner = closeRunner;
  }

  synchronized PgPool client() {
    if (client == null) {
      client = new PgPoolImpl(vertx, false, options);
    }
    return client;
  }

  synchronized void incRefCount() {
    refCount++;
  }

  synchronized void close() {
    if (--refCount == 0) {
      if (client != null) {
        client.close();
      }
      if (closeRunner != null) {
        closeRunner.run();
      }
    }
  }
}



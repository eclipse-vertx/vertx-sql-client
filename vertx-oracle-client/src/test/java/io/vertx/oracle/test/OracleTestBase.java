/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.oracle.test;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class OracleTestBase {

  public static Vertx vertx;

  @BeforeClass
  public static void start() {
    vertx = Vertx.vertx();
  }

  @AfterClass
  public static void stop() {
    await(vertx.close());
  }

  public static <T> T await(Future<T> future) {
    try {
      return future.toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted before receiving a result");
    } catch (ExecutionException | TimeoutException e) {
      throw new RuntimeException(e);
    }
  }

}

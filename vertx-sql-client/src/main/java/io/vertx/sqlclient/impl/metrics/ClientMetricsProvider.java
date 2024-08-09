/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.sqlclient.impl.metrics;

import io.vertx.core.Closeable;
import io.vertx.core.Promise;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.sqlclient.SqlConnectOptions;

/**
 * Provides client metrics for a connection
 */
public interface ClientMetricsProvider extends Closeable {

  ClientMetrics<?, ?, ?, ?> metricsFor(SqlConnectOptions options);

  @Override
  default void close(Promise<Void> completion) {
    try {
      close();
    } catch (Exception e) {
      completion.fail(e);
      return;
    }
    completion.complete();
  }

  void close();

}

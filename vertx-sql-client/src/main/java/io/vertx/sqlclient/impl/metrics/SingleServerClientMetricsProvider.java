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

import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.sqlclient.SqlConnectOptions;

/**
 * A metrics provider for a single database for which we can reasonably record queue metrics.
 */
public final class SingleServerClientMetricsProvider implements ClientMetricsProvider {

  private final UncloseableClientMetrics<?, ?, ?, ?> metrics;

  public SingleServerClientMetricsProvider(ClientMetrics<?, ?, ?, ?> metrics) {
    this.metrics = new UncloseableClientMetrics<>(metrics);
  }

  public ClientMetrics<?, ?, ?, ?> metrics() {
    return metrics;
  }

  @Override
  public ClientMetrics<?, ?, ?, ?> metricsFor(SqlConnectOptions options) {
    return metrics;
  }

  @Override
  public void close() {
    metrics.actual.close();
  }

  static class UncloseableClientMetrics<M, T, Req, Resp> extends ClientMetricsWrapper<M, T, Req, Resp> {
    public UncloseableClientMetrics(ClientMetrics<M, T, Req, Resp> actual) {
      super(actual);
    }
  }
}

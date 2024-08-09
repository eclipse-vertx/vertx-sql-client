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

import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.sqlclient.SqlConnectOptions;

import java.util.*;

/**
 * A metrics provider that is not coupled to a single database.
 */
public final class DynamicClientMetricsProvider implements ClientMetricsProvider {

  private final Map<SocketAddress, Wrapper<?, ?, ?, ?>> metricsMap = new HashMap<>();
  private final VertxMetrics metrics;
  private boolean closed;

  public DynamicClientMetricsProvider(VertxMetrics metrics) {
    this.metrics = metrics;
  }

  public ClientMetrics<?, ?, ?, ?> metricsFor(SqlConnectOptions options) {
    SocketAddress address = options.getSocketAddress();
    synchronized (this) {
      if (closed) {
        return null;
      }
      Wrapper<?, ?, ?, ?> wrapper = metricsMap.get(address);
      if (wrapper == null) {
        ClientMetrics<?, ?, ?, ?> actual = metrics.createClientMetrics(address, "sql", options.getMetricsName());
        wrapper = new Wrapper<>(address, this, actual);
        metricsMap.put(address, wrapper);
      }
      return wrapper;
    }
  }

  @Override
  public void close() {
    List<ClientMetrics<?, ?, ?, ?>> toClose = new ArrayList<>();
    synchronized (this) {
      if (closed) {
        return;
      }
      closed = true;
      for (Wrapper<?, ?, ?, ?> wrapper : metricsMap.values()) {
        ClientMetrics<?, ?, ?, ?> actual = wrapper.doClose();
        if (actual != null) {
          toClose.add(actual);
        }
      }
      metricsMap.clear();
    }
    for (ClientMetrics<?, ?, ?, ?> metrics : toClose) {
      metrics.close();
    }
  }

  private static class Wrapper<M, T, Req, Resp> extends ClientMetricsWrapper<M, T, Req, Resp> {

    private final SocketAddress socketAddress;
    private final DynamicClientMetricsProvider provider;
    private int refCount;

    public Wrapper(SocketAddress socketAddress, DynamicClientMetricsProvider provider, ClientMetrics<M, T, Req, Resp> actualMetrics) {
      super(actualMetrics);
      this.socketAddress = socketAddress;
      this.provider = provider;
      this.refCount = 1;
    }

    // Call under sync
    ClientMetrics<?, ?, ?, ?> doClose() {
      if (refCount > 0) {
        refCount = 0;
        return actual;
      } else {
        return null;
      }
    }

    @Override
    public void close() {
      synchronized (provider) {
        if (--refCount > 0) {
          return;
        }
        provider.metricsMap.remove(socketAddress);
      }
      actual.close();
    }
  }
}

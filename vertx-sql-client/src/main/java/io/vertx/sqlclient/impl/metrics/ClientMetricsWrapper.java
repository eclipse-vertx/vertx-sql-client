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

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.spi.metrics.ClientMetrics;

/**
 * Wraps an {@link #actual} wrapper, metrics operations are delegated, error are logged using the {@link ClientMetricsProvider} name.
 */
public class ClientMetricsWrapper<M, T, Req, Resp> implements ClientMetrics<M, T, Req, Resp> {

  private static final Logger logger = LoggerFactory.getLogger(ClientMetricsProvider.class);

  protected final ClientMetrics<M, T, Req, Resp> actual;

  public ClientMetricsWrapper(ClientMetrics<M, T, Req, Resp> actual) {
    this.actual = actual;
  }

  @Override
  public T enqueueRequest() {
    try {
      return actual.enqueueRequest();
    } catch (Exception e) {
      logger.error("Metrics failure", e);
    }
    return null;
  }
  @Override
  public void dequeueRequest(T taskMetric) {
    try {
      actual.dequeueRequest(taskMetric);
    } catch (Exception e) {
      logger.error("Metrics failure", e);
    }
  }
  @Override
  public M requestBegin(String uri, Req request) {
    try {
      return actual.requestBegin(uri, request);
    } catch (Exception e) {
      logger.error("Metrics failure", e);
    }
    return null;
  }
  @Override
  public void requestEnd(M requestMetric) {
    try {
      actual.requestEnd(requestMetric);
    } catch (Exception e) {
      logger.error("Metrics failure", e);
    }
  }
  @Override
  public void requestEnd(M requestMetric, long bytesWritten) {
    try {
      actual.requestEnd(requestMetric, bytesWritten);
    } catch (Exception e) {
      logger.error("Metrics failure", e);
    }
  }
  @Override
  public void responseBegin(M requestMetric, Resp response) {
    try {
      actual.responseBegin(requestMetric, response);
    } catch (Exception e) {
      logger.error("Metrics failure", e);
    }
  }
  @Override
  public void requestReset(M requestMetric) {
    try {
      actual.requestReset(requestMetric);
    } catch (Exception e) {
      logger.error("Metrics failure", e);
    }
  }
  @Override
  public void responseEnd(M requestMetric) {
    try {
      actual.responseEnd(requestMetric);
    } catch (Exception e) {
      logger.error("Metrics failure", e);
    }
  }
  @Override
  public void responseEnd(M requestMetric, long bytesRead) {
    try {
      actual.responseEnd(requestMetric, bytesRead);
    } catch (Exception e) {
      logger.error("Metrics failure", e);
    }
  }
}

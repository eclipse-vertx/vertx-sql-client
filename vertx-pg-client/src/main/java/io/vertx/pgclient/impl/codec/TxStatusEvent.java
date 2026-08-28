/*
 * Copyright (c) 2011-2025 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.pgclient.impl.codec;

import io.vertx.sqlclient.impl.TransactionState;

public class TxStatusEvent {

  public static final TxStatusEvent IDLE = new TxStatusEvent(TransactionState.IDLE);
  public static final TxStatusEvent ACTIVE = new TxStatusEvent(TransactionState.ACTIVE);
  public static final TxStatusEvent FAILED = new TxStatusEvent(TransactionState.FAILED);

  private final TransactionState status;

  private TxStatusEvent(TransactionState status) {
    this.status = status;
  }

  public TransactionState status() {
    return status;
  }
}

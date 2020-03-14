/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl.protocol;

public enum MessageType {

  SQL_BATCH(1),
  PRE_TDS7_LOGIN(2),
  RPC(3),
  TABULAR_RESULT(4),
  ATTENTION_SIGNAL(6),
  BULK_LOAD_DATA(7),
  FEDERATED_AUTHENTICATION_TOKEN(8),
  TRANSACTION_MANAGER_REQUEST(14),
  TDS7_LOGIN(16),
  SSPI(17),
  PRE_LOGIN(18);

  private final int value;

  MessageType(int value) {
    this.value = value;
  }

  public static MessageType valueOf(int value) {
    switch (value) {
      case 1:
        return SQL_BATCH;
      case 2:
        return PRE_TDS7_LOGIN;
      case 3:
        return RPC;
      case 4:
        return TABULAR_RESULT;
      case 6:
        return ATTENTION_SIGNAL;
      case 7:
        return BULK_LOAD_DATA;
      case 8:
        return FEDERATED_AUTHENTICATION_TOKEN;
      case 14:
        return TRANSACTION_MANAGER_REQUEST;
      case 16:
        return TDS7_LOGIN;
      case 17:
        return SSPI;
      case 18:
        return PRE_LOGIN;
      default:
        throw new IllegalArgumentException("Unknown message type value");
    }
  }

  public int value() {
    return this.value;
  }
}

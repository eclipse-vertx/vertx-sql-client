/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl.protocol;

public enum MessageStatus {

  NORMAL(0x00),
  END_OF_MESSAGE(0x01),
  IGNORE_THIS_EVENT(0x02),
  RESET_CONNECTION(0x08),
  RESET_CONNECTION_SKIP_TRAN(0x10);

  private final int value;

  MessageStatus(int value) {
    this.value = value;
  }

  public static MessageStatus valueOf(int value) {
    switch (value) {
      case 0x00:
        return NORMAL;
      case 0x01:
        return END_OF_MESSAGE;
      case 0x02:
        return IGNORE_THIS_EVENT;
      case 0x08:
        return RESET_CONNECTION;
      case 0x10:
        return RESET_CONNECTION_SKIP_TRAN;
      default:
        throw new IllegalArgumentException("Unknown message status value");
    }
  }

  public int value() {
    return this.value;
  }
}

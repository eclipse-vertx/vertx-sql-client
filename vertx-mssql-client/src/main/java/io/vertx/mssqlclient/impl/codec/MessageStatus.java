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

package io.vertx.mssqlclient.impl.codec;

@SuppressWarnings("unused")
public class MessageStatus {

  public static final short NORMAL = 0x00;
  public static final short END_OF_MESSAGE = 0x01;
  public static final short IGNORE_THIS_EVENT = 0x02;
  public static final short RESET_CONNECTION = 0x08;
  public static final short RESET_CONNECTION_SKIP_TRAN = 0x10;

  private MessageStatus() {
    // Constants class
  }
}

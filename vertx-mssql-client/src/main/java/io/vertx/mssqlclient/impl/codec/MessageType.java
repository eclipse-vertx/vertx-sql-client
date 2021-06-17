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
public class MessageType {

  public static final int SQL_BATCH = 1;
  public static final int PRE_TDS7_LOGIN = 2;
  public static final int RPC = 3;
  public static final int TABULAR_RESULT = 4;
  public static final int ATTENTION_SIGNAL = 6;
  public static final int BULK_LOAD_DATA = 7;
  public static final int FEDERATED_AUTHENTICATION_TOKEN = 8;
  public static final int TRANSACTION_MANAGER_REQUEST = 14;
  public static final int TDS7_LOGIN = 16;
  public static final int SSPI = 17;
  public static final int PRE_LOGIN = 18;

  private MessageType() {
    // Constants class
  }
}

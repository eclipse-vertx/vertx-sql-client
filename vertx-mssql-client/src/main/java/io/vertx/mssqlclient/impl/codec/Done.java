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
public class Done {

  public static final short STATUS_DONE_FINAL = 0x00;
  public static final short STATUS_DONE_MORE = 0x1;
  public static final short STATUS_DONE_ERROR = 0x2;
  public static final short STATUS_DONE_DONE_INXACT = 0x4;
  public static final short STATUS_DONE_COUNT = 0x10;
  public static final short STATUS_DONE_ATTN = 0x20;
  public static final short STATUS_DONE_SRVERROR = 0x100;

  private Done() {
    // Constants class
  }
}

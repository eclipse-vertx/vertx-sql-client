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

public class EncryptionLevel {

  public static final byte ENCRYPT_OFF = 0x00;
  public static final byte ENCRYPT_ON = 0x01;
  public static final byte ENCRYPT_NOT_SUP = 0x02;
  public static final byte ENCRYPT_REQ = 0x03;

  private EncryptionLevel() {
    // Constants class
  }
}

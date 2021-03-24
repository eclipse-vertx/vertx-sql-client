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

package io.vertx.mssqlclient.impl.protocol.token;

import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;

public enum DataPacketStreamTokenType {

  ALTMETADATA_TOKEN(0x88),
  ALTROW_TOKEN(0xD3),
  COLMETADATA_TOKEN(0x81),
  COLINFO_TOKEN(0xA5),
  DONE_TOKEN(0xFD),
  DONEPROC_TOKEN(0xFE),
  DONEINPROC_TOKEN(0xFF),
  ENVCHANGE_TOKEN(0xE3),
  ERROR_TOKEN(0xAA),
  FEATUREEXTACK(0xAE),
  FEDAUTHINFO_TOKEN(0xEE),
  INFO_TOKEN(0xAB),
  LOGINACK_TOKEN(0xAD),
  NBCROW_TOKEN(0xD2),
  ORDER_TOKEN(0xA9),
  RETURNSTATUS_TOKEN(0x79),
  RETURNVALUE_TOKEN(0xAC),
  ROW_TOKEN(0xD1),
  SESSIONSTATE_TOKEN(0xE4),
  SSPI_TOKEN(0xED),
  TABNAME_TOKEN(0xA4),
  OFFSET_TOKEN(0x78);

  private final int value;

  private static final IntObjectMap<DataPacketStreamTokenType> lookup;

  static {
    IntObjectMap<DataPacketStreamTokenType> map = new IntObjectHashMap<>();
    for (DataPacketStreamTokenType dataPacketStreamTokenType : DataPacketStreamTokenType.values()) {
      if (map.put(dataPacketStreamTokenType.value(), dataPacketStreamTokenType) != null) {
        throw new IllegalStateException("Duplicate key");
      }
    }
    lookup = map;
  }

  DataPacketStreamTokenType(int value) {
    this.value = value;
  }

  public static DataPacketStreamTokenType valueOf(int value) {
    return lookup.get(value);
  }

  public int value() {
    return value;
  }
}

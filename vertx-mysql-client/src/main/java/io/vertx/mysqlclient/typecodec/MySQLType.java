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

package io.vertx.mysqlclient.typecodec;

public enum MySQLType {
  UNKNOWN(0x00), // this is for internal use
  TINYINT(0x01),
  UNSIGNED_TINYINT(0x02),
  SMALLINT(0x03),
  UNSIGNED_SMALLINT(0x04),
  MEDIUMINT(0x05),
  UNSIGNED_MEDIUMINT(0x06),
  INT(0x07),
  UNSIGNED_INT(0x08),
  BIGINT(0x09),
  UNSIGNED_BIGINT(0x10),
  DOUBLE(0x11),
  FLOAT(0x12),
  BIT(0x13),
  NUMERIC(0x14),

  DATE(0x15),
  TIME(0x16),
  DATETIME(0x17),
  TIMESTAMP(0x18),
  YEAR(0x19),

  STRING(0x20),
  BINARY_STRING(0x21),
  VARSTRING(0x22),
  BINARY_VARSTRING(0x23),
  TEXT(0x24),
  BLOB(0x25),

  JSON(0x26),
  GEOMETRY(0x27);

  private final int identifier;

  MySQLType(int identifier) {
    this.identifier = identifier;
  }

  public int identifier() {
    return identifier;
  }
}

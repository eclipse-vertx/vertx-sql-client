/*
 * Copyright (c) 2011-2023 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.oracleclient.impl;

import io.vertx.sqlclient.spi.DatabaseMetadata;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class OracleMetadata implements DatabaseMetadata {

  private final String productName;
  private final String fullVersion;
  private final int majorVersion;
  private final int minorVersion;

  public OracleMetadata(DatabaseMetaData metadata) throws SQLException {
    productName = metadata.getDatabaseProductName();
    fullVersion = metadata.getDatabaseProductVersion();
    majorVersion = metadata.getDatabaseMajorVersion();
    minorVersion = metadata.getDatabaseMinorVersion();
  }

  @Override
  public String productName() {
    return productName;
  }

  @Override
  public String fullVersion() {
    return fullVersion;
  }

  @Override
  public int majorVersion() {
    return majorVersion;
  }

  @Override
  public int minorVersion() {
    return minorVersion;
  }
}

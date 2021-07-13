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
package io.vertx.oracle.impl;

import io.vertx.sqlclient.spi.DatabaseMetadata;

import java.sql.DatabaseMetaData;

public class OracleMetadataImpl implements DatabaseMetadata {
  private final DatabaseMetaData metadata;

  public OracleMetadataImpl(DatabaseMetaData metadata) {
    this.metadata = metadata;
  }

  @Override
  public String productName() {
    return Helper.getOrHandleSQLException(metadata::getDatabaseProductName);
  }

  @Override
  public String fullVersion() {
    return Helper.getOrHandleSQLException(metadata::getDatabaseProductVersion);
  }

  @Override
  public int majorVersion() {
    return Helper.getOrHandleSQLException(metadata::getDatabaseMajorVersion);
  }

  @Override
  public int minorVersion() {
    return Helper.getOrHandleSQLException(metadata::getDatabaseMinorVersion);
  }
}

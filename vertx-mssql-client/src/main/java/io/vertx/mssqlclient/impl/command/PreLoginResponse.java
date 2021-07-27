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

package io.vertx.mssqlclient.impl.command;

import io.vertx.mssqlclient.impl.MSSQLDatabaseMetadata;

import java.util.Objects;

public class PreLoginResponse {

  private final MSSQLDatabaseMetadata metadata;
  private final Byte encryptionLevel;

  public PreLoginResponse(MSSQLDatabaseMetadata metadata, Byte encryptionLevel) {
    this.metadata = Objects.requireNonNull(metadata);
    this.encryptionLevel = Objects.requireNonNull(encryptionLevel);
  }

  public MSSQLDatabaseMetadata metadata() {
    return metadata;
  }

  public Byte encryptionLevel() {
    return encryptionLevel;
  }
}

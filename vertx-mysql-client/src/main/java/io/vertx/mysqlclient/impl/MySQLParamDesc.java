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

package io.vertx.mysqlclient.impl;

import io.vertx.mysqlclient.impl.protocol.ColumnDefinition;
import io.vertx.sqlclient.impl.ParamDesc;

public class MySQLParamDesc extends ParamDesc {
  private final ColumnDefinition[] paramDefinitions;

  public MySQLParamDesc(ColumnDefinition[] paramDefinitions) {
    this.paramDefinitions = paramDefinitions;
  }

  public ColumnDefinition[] paramDefinitions() {
    return paramDefinitions;
  }
}

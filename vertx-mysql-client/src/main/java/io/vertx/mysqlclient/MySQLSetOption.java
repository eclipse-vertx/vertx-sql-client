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

package io.vertx.mysqlclient;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;

/**
 * MySQL set options which can be used by {@link MySQLConnection#setOption(MySQLSetOption)}.
 */
@VertxGen
public enum MySQLSetOption {
  MYSQL_OPTION_MULTI_STATEMENTS_ON, MYSQL_OPTION_MULTI_STATEMENTS_OFF
}

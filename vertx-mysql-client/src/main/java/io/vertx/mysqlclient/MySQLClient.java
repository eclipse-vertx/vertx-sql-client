/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
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
import io.vertx.sqlclient.PropertyKind;

/**
 * An interface to define MySQL specific constants or behaviors.
 */
@VertxGen
public interface MySQLClient {
  /**
   * SqlResult {@link PropertyKind property kind} for MySQL last_insert_id.<br>
   * The property kind can be used to fetch the auto incremented id of the last row when executing inserting or updating operations.
   */
  PropertyKind<Long> LAST_INSERTED_ID = () -> Long.class;
}

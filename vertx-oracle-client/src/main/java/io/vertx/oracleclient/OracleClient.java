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

package io.vertx.oracleclient;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.sqlclient.PropertyKind;
import io.vertx.sqlclient.Row;

/**
 * An interface to define Oracle specific constants or behaviors.
 */
@VertxGen
public interface OracleClient {

  /**
   * The property to be used to retrieve the generated keys
   */
  PropertyKind<Row> GENERATED_KEYS = PropertyKind.create("generated-keys", Row.class);

  /**
   * The property to be used to retrieve the output of the callable statement
   */
  PropertyKind<Boolean> OUTPUT = PropertyKind.create("callable-statement-output", Boolean.class);
}

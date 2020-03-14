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

package io.vertx.sqlclient;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;

/**
 * The kind of the property, this can be used to fetch some specific property of the {@link SqlResult execution result}.
 */
@VertxGen
public interface PropertyKind<T> {
  /**
   * Get the type of the value of this kind of property.
   *
   * @return the type
   */
  @GenIgnore
  Class<T> type();
}

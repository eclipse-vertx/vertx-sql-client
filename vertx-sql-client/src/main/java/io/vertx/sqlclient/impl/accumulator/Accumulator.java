/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.sqlclient.impl.accumulator;

import io.vertx.sqlclient.RowIterator;

import java.util.function.Consumer;

/**
 * A data structure similar to a {@link java.util.Collection} but which is append-only.
 */
public interface Accumulator<T> extends Consumer<T>, Iterable<T> {
  @Override
  RowIterator<T> iterator();
}

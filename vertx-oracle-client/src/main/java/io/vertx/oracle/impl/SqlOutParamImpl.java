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

import io.vertx.oracle.SqlOutParam;

public class SqlOutParamImpl implements SqlOutParam {

  private final Object value;
  private final int type;
  private final boolean in;

  public SqlOutParamImpl(Object value, int type) {
    this.value = value;
    this.type = type;
    in = true;
  }

  public SqlOutParamImpl(int type) {
    this.value = null;
    this.type = type;
    in = false;
  }

  @Override
  public boolean in() {
    return in;
  }

  @Override
  public int type() {
    return type;
  }

  @Override
  public Object value() {
    return value;
  }
}

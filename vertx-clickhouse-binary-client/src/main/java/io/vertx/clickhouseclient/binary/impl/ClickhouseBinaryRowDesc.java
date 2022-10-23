/*
 *
 *  Copyright (c) 2021 Vladimir Vishnevskii
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Eclipse Public License 2.0 which is available at
 *  http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 *  which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package io.vertx.clickhouseclient.binary.impl;

import io.vertx.clickhouseclient.binary.impl.codec.ClickhouseBinaryColumnDescriptor;
import io.vertx.sqlclient.impl.RowDesc;

public class ClickhouseBinaryRowDesc extends RowDesc {
  public static final ClickhouseBinaryColumnDescriptor[] EMPTY_DESCRIPTORS = new ClickhouseBinaryColumnDescriptor[0];
  public static final ClickhouseBinaryRowDesc EMPTY = new ClickhouseBinaryRowDesc(EMPTY_DESCRIPTORS);

  public ClickhouseBinaryRowDesc(ClickhouseBinaryColumnDescriptor[] columnDescriptors) {
    super(columnDescriptors);
  }
}

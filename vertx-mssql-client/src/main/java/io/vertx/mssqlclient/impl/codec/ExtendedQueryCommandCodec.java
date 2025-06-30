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

package io.vertx.mssqlclient.impl.codec;

import io.vertx.sqlclient.internal.TupleInternal;
import io.vertx.sqlclient.internal.command.ExtendedQueryCommand;

class ExtendedQueryCommandCodec<T> extends ExtendedQueryCommandBaseCodec<T> {

  ExtendedQueryCommandCodec(ExtendedQueryCommand<T> cmd) {
    super(cmd);
  }

  @Override
  protected TupleInternal prepexecRequestParams() {
    return cmd.params();
  }

  @Override
  protected TupleInternal execRequestParams() {
    return cmd.params();
  }
}

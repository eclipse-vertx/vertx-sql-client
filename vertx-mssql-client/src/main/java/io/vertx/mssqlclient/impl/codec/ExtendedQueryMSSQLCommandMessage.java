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

import io.vertx.sqlclient.internal.TupleBase;
import io.vertx.sqlclient.spi.protocol.ExtendedQueryCommand;

class ExtendedQueryMSSQLCommandMessage<T> extends ExtendedQueryMSSQLCommandBaseMessage<T> {

  ExtendedQueryMSSQLCommandMessage(ExtendedQueryCommand<T> cmd, MSSQLPreparedStatement ps) {
    super(cmd, ps);
  }

  @Override
  protected TupleBase prepexecRequestParams() {
    return cmd.params();
  }

  @Override
  protected TupleBase execRequestParams() {
    return cmd.params();
  }
}

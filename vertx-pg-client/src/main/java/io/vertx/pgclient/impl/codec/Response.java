/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.pgclient.impl.codec;

import io.vertx.codegen.annotations.VertxGen;

/**
 *
 * <p>
 * A common response message for PostgreSQL
 * <a href="https://www.postgresql.org/docs/9.5/static/protocol-error-fields.html">Error and Notice Message Fields</a>
 */
@VertxGen
public interface Response {

  public String getSeverity();

  public String getCode();

  public String getMessage();

  public String getDetail();

  public String getHint();

  public String getPosition();

  public String getWhere();

  public String getFile();

  public String getLine();

  public String getRoutine();

  public String getSchema();

  public String getTable();

  public String getColumn();

  public String getDataType();

  public String getConstraint();

  public String getInternalPosition();

  public String getInternalQuery();
}

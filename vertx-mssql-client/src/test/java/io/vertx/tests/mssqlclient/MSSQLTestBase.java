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

package io.vertx.tests.mssqlclient;

import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.tests.mssqlclient.junit.MSSQLRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;

public abstract class MSSQLTestBase {

  @ClassRule
  public static MSSQLRule rule = MSSQLRule.SHARED_INSTANCE;

  protected static MSSQLConnectOptions options;

  @BeforeClass
  public static void before() {
    options = rule.options();
  }
}

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

package io.vertx.tests.mssqlclient.data;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(VertxUnitRunner.class)
public class MSSQLGuidDataTypeTest extends MSSQLDataTypeTestBase {

  @Test
  public void testQueryGuid(TestContext ctx) {
    testQueryDecodeGenericWithoutTable(ctx, "test_guid", "UNIQUEIDENTIFIER", "'bda9b971-57a8-4216-877b-5cd24b9bb47f'", UUID.fromString("bda9b971-57a8-4216-877b-5cd24b9bb47f"));
  }

  @Test
  public void testQueryGuidWithParans(TestContext ctx) {
    testQueryDecodeGenericWithoutTable(ctx, "test_guid", "UNIQUEIDENTIFIER", "'{5d0201ed-289c-41bd-a533-937627ce1d60}'", UUID.fromString("5d0201ed-289c-41bd-a533-937627ce1d60"));
  }

  @Test
  public void testPreparedQueryGuid(TestContext ctx) {
    testPreparedQueryDecodeGenericWithoutTable(ctx, "test_guid", "UNIQUEIDENTIFIER", "'70a1a8dc-c817-41c2-a70f-54fd71172b38'", UUID.fromString("70a1a8dc-c817-41c2-a70f-54fd71172b38"));
  }

  @Test
  public void testPreparedQueryGuidWithParans(TestContext ctx) {
    testPreparedQueryDecodeGenericWithoutTable(ctx, "test_guid", "UNIQUEIDENTIFIER", "'{19e5bec7-fe8f-4810-98d3-0a073349e323}'", UUID.fromString("19e5bec7-fe8f-4810-98d3-0a073349e323"));
  }

}

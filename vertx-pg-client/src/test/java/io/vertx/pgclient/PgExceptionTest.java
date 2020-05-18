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

package io.vertx.pgclient;

import org.junit.Test;

import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.impl.codec.ResponseHelper;

import static org.junit.Assert.*;

public class PgExceptionTest {

  @Test
  public void fieldConstructor() {
    PgException pgException = new PgException("myMessage", "mySeverity", "myCode", "myDetail");
    assertEquals("myMessage", pgException.getErrorMessage());
    assertEquals("mySeverity", pgException.getSeverity());
    assertEquals("myCode", pgException.getCode());
    assertEquals("myDetail", pgException.getDetail());

    JsonObject jsonObject = new JsonObject(pgException.getMessage());
    assertEquals("myMessage", jsonObject.getString("message"));
    assertEquals("mySeverity", jsonObject.getString("severity"));
    assertEquals("myCode", jsonObject.getString("code"));
    assertEquals("myDetail", jsonObject.getString("detail"));
  }

  @Test
  public void nullFieldConstructor() {
    assertEquals("{}", new PgException(null, null, null, null).getMessage());
  }

  @Test
  public void errorResponseConstructor() {
    PgException pgException = ResponseHelper.getCompletePgException();
    assertEquals("myMessage", pgException.getErrorMessage());
    assertEquals("mySeverity", pgException.getSeverity());
    assertEquals("myCode", pgException.getCode());
    assertEquals("myDetail", pgException.getDetail());

    // getMessage() should return a valid JsonObject with all fields
    JsonObject jsonObject = new JsonObject(pgException.getMessage());
    assertEquals("myMessage", jsonObject.getString("message"));
    assertEquals("mySeverity", jsonObject.getString("severity"));
    assertEquals("myCode", jsonObject.getString("code"));
    assertEquals("myDetail", jsonObject.getString("detail"));
    assertEquals("myHint", jsonObject.getString("hint"));
    assertEquals("myPosition", jsonObject.getString("position"));
    assertEquals("myInternalPosition", jsonObject.getString("internalPosition"));
    assertEquals("myInternalQuery", jsonObject.getString("internalQuery"));
    assertEquals("myWhere", jsonObject.getString("where"));
    assertEquals("myFile", jsonObject.getString("file"));
    assertEquals("myLine", jsonObject.getString("line"));
    assertEquals("myRoutine", jsonObject.getString("routine"));
    assertEquals("mySchema", jsonObject.getString("schema"));
    assertEquals("myTable", jsonObject.getString("table"));
    assertEquals("myColumn", jsonObject.getString("column"));
    assertEquals("myDataType", jsonObject.getString("dataType"));
    assertEquals("myConstraint", jsonObject.getString("constraint"));
  }

  @Test
  public void nullErrorResponseConstructor() {
    assertEquals("{}", ResponseHelper.getEmptyPgException().getMessage());
  }
}

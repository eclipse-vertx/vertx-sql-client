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

import io.vertx.pgclient.PgException;

public final class ResponseHelper {

  public static PgException getCompletePgException() {
    ErrorResponse errorResponse = new ErrorResponse();
    errorResponse.setMessage("myMessage");
    errorResponse.setSeverity("mySeverity");
    errorResponse.setCode("myCode");
    errorResponse.setDetail("myDetail");
    errorResponse.setHint("myHint");
    errorResponse.setPosition("myPosition");
    errorResponse.setHint("myHint");
    errorResponse.setPosition("myPosition");
    errorResponse.setInternalPosition("myInternalPosition");
    errorResponse.setInternalQuery("myInternalQuery");
    errorResponse.setWhere("myWhere");
    errorResponse.setFile("myFile");
    errorResponse.setLine("myLine");
    errorResponse.setRoutine("myRoutine");
    errorResponse.setSchema("mySchema");
    errorResponse.setTable("myTable");
    errorResponse.setColumn("myColumn");
    errorResponse.setDataType("myDataType");
    errorResponse.setConstraint("myConstraint");
    return errorResponse.toException();
  }

  public static PgException getEmptyPgException() {
    return new ErrorResponse().toException();
  }
}

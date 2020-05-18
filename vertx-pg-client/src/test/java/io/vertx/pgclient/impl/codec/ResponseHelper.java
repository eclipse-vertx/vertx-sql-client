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

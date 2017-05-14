package io.vertx.pgclient.codec.decoder.message;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class ErrorResponse extends Response {

  @Override
  public String toString() {
    return "ErrorResponse{" +
      "severity='" + getSeverity() + '\'' +
      ", code='" + getCode() + '\'' +
      ", message='" + getMessage() + '\'' +
      ", detail='" + getDetail() + '\'' +
      ", hint='" + getHint() + '\'' +
      ", position='" + getPosition() + '\'' +
      ", internalPosition='" + getInternalPosition() + '\'' +
      ", internalQuery='" + getInternalQuery() + '\'' +
      ", where='" + getWhere() + '\'' +
      ", file='" + getFile() + '\'' +
      ", line='" + getLine() + '\'' +
      ", routine='" + getRoutine() + '\'' +
      ", schema='" + getSchema() + '\'' +
      ", table='" + getTable() + '\'' +
      ", column='" + getColumn() + '\'' +
      ", dataType='" + getDataType() + '\'' +
      ", constraint='" + getConstraint() + '\'' +
      '}';
  }
}

package io.vertx.pgclient.codec.decoder.message;

public class NoticeResponseMessage extends ResponseMessage {
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("NoticeResponseMessage{");
    sb.append("severity='").append(getSeverity()).append('\'');
    sb.append(", code='").append(getCode()).append('\'');
    sb.append(", message='").append(getMessage()).append('\'');
    sb.append(", detail='").append(getDetail()).append('\'');
    sb.append(", hint='").append(getHint()).append('\'');
    sb.append(", position='").append(getPosition()).append('\'');
    sb.append(", where='").append(getWhere()).append('\'');
    sb.append(", file='").append(getFile()).append('\'');
    sb.append(", line='").append(getLine()).append('\'');
    sb.append(", routine='").append(getRoutine()).append('\'');
    sb.append(", schema='").append(getSchema()).append('\'');
    sb.append(", table='").append(getTable()).append('\'');
    sb.append(", column='").append(getColumn()).append('\'');
    sb.append(", dataType='").append(getDataType()).append('\'');
    sb.append(", constraint='").append(getConstraint()).append('\'');
    sb.append('}');
    return sb.toString();
  }
}

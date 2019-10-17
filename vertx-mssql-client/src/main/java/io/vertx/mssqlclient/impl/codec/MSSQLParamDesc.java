package io.vertx.mssqlclient.impl.codec;

import io.vertx.mssqlclient.impl.protocol.datatype.MSSQLDataType;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.ParamDesc;
import io.vertx.sqlclient.impl.TupleInternal;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class MSSQLParamDesc extends ParamDesc {
  private final ColumnData[] paramDescriptions;

  public MSSQLParamDesc(ColumnData[] paramDescriptions) {
    this.paramDescriptions = paramDescriptions;
  }

  public ColumnData[] paramDescriptions() {
    return paramDescriptions;
  }

  @Override
  public String prepare(TupleInternal values) {
    if (values.size() != paramDescriptions.length){
      return buildReport(values);
    }
    return null;
  }

  // reuse from pg
  private String buildReport(Tuple values) {
    List<Object> checkList = new ArrayList<>(values.size());
    for (int i = 0; i < values.size(); i++) {
      checkList.add(values.getValue(i));
    }
    Stream<Class> types = Stream.of(paramDescriptions).map(ColumnData::dataType).map(MSSQLDataType::mappedJavaType);
    return "Values [" + checkList.stream().map(String::valueOf).collect(Collectors.joining(", ")) +
      "] cannot be coerced to [" + types
      .map(Class::getSimpleName)
      .collect(Collectors.joining(", ")) + "]";
  }
}

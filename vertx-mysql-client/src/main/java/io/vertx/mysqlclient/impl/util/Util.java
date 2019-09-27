package io.vertx.mysqlclient.impl.util;

import io.vertx.sqlclient.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Util {

  public static String buildInvalidArgsError(Tuple values, Stream<Class> types) {
    List<Object> tmp = new ArrayList<>(values.size());
    for (int i = 0;i < values.size();i++) {
      tmp.add(values.getValue(i));
    }
    return "Values [" + tmp.stream().map(String::valueOf).collect(Collectors.joining(", ")) +
      "] cannot be coerced to [" + types
      .map(Class::getSimpleName)
      .collect(Collectors.joining(", ")) + "]";
  }
}

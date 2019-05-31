package io.vertx.mysqlclient.impl.util;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Util {

  public static String buildInvalidArgsError(Stream<Object> values, Stream<Class> types) {
    return "Values [" + values.map(String::valueOf).collect(Collectors.joining(", ")) +
      "] cannot be coerced to [" + types
      .map(Class::getSimpleName)
      .collect(Collectors.joining(", ")) + "]";
  }
}

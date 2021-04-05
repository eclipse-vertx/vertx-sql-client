package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum EnumResolutionMethod {
  NAME("by_name"), ORDINAL("by_ordinal"), KEY("by_key");
  private static final Map<String, EnumResolutionMethod> index = Arrays
    .stream(EnumResolutionMethod.values())
    .collect(Collectors.toMap(el -> el.optVal, el -> el));

  String optVal;

  EnumResolutionMethod(String optVal) {
    this.optVal = optVal;
  }

  public static EnumResolutionMethod forOpt(String optVal) {
    EnumResolutionMethod ret = index.get(optVal);
    if (ret == null) {
      throw new IllegalArgumentException("unknown option value " + optVal);
    }
    return ret;
  }
}

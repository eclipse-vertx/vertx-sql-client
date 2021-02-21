package io.vertx.clickhouse.clickhousenative.impl.codec;

public class QueryProcessingStage {
  public static final int FETCH_COLUMNS = 0;
  public static final int WITH_MERGEABLE_STATE = 1;
  public static final int COMPLETE = 2;
}

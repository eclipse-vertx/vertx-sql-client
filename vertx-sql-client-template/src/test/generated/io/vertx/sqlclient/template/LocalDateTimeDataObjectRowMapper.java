package io.vertx.sqlclient.template;

/**
 * Mapper for {@link LocalDateTimeDataObject}.
 * NOTE: This class has been automatically generated from the {@link LocalDateTimeDataObject} original class using Vert.x codegen.
 */
public class LocalDateTimeDataObjectRowMapper implements java.util.function.Function<io.vertx.sqlclient.Row, LocalDateTimeDataObject> {

  public static final java.util.function.Function<io.vertx.sqlclient.Row, LocalDateTimeDataObject> INSTANCE = new LocalDateTimeDataObjectRowMapper();

  public static final java.util.stream.Collector<io.vertx.sqlclient.Row, ?, java.util.List<LocalDateTimeDataObject>> COLLECTOR = java.util.stream.Collectors.mapping(INSTANCE, java.util.stream.Collectors.toList());

  public LocalDateTimeDataObject apply(io.vertx.sqlclient.Row row) {
    LocalDateTimeDataObject obj = new LocalDateTimeDataObject();
    Object val;
    val = row.getLocalDateTime("localDateTime");
    if (val != null) {
      obj.setLocalDateTime((java.time.LocalDateTime)val);
    }
    return obj;
  }
}

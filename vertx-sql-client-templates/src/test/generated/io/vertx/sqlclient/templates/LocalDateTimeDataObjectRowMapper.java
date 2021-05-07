package io.vertx.sqlclient.templates;

/**
 * Mapper for {@link LocalDateTimeDataObject}.
 * NOTE: This class has been automatically generated from the {@link LocalDateTimeDataObject} original class using Vert.x codegen.
 */
@io.vertx.codegen.annotations.VertxGen
public interface LocalDateTimeDataObjectRowMapper extends io.vertx.sqlclient.templates.RowMapper<LocalDateTimeDataObject> {

  @io.vertx.codegen.annotations.GenIgnore
  LocalDateTimeDataObjectRowMapper INSTANCE = new LocalDateTimeDataObjectRowMapper() { };

  @io.vertx.codegen.annotations.GenIgnore
  java.util.stream.Collector<io.vertx.sqlclient.Row, ?, java.util.List<LocalDateTimeDataObject>> COLLECTOR = java.util.stream.Collectors.mapping(INSTANCE::map, java.util.stream.Collectors.toList());

  @io.vertx.codegen.annotations.GenIgnore
  default LocalDateTimeDataObject map(io.vertx.sqlclient.Row row) {
    LocalDateTimeDataObject obj = new LocalDateTimeDataObject();
    Object val;
    int idx;
    if ((idx = row.getColumnIndex("localDateTime")) != -1 && (val = row.getLocalDateTime(idx)) != null) {
      obj.setLocalDateTime((java.time.LocalDateTime)val);
    }
    return obj;
  }
}

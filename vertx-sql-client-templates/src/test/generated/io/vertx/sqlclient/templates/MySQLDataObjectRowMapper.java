package io.vertx.sqlclient.templates;

/**
 * Mapper for {@link MySQLDataObject}.
 * NOTE: This class has been automatically generated from the {@link MySQLDataObject} original class using Vert.x codegen.
 */
@io.vertx.codegen.annotations.VertxGen
public interface MySQLDataObjectRowMapper extends io.vertx.sqlclient.templates.RowMapper<MySQLDataObject> {

  @io.vertx.codegen.annotations.GenIgnore
  MySQLDataObjectRowMapper INSTANCE = new MySQLDataObjectRowMapper() { };

  @io.vertx.codegen.annotations.GenIgnore
  java.util.stream.Collector<io.vertx.sqlclient.Row, ?, java.util.List<MySQLDataObject>> COLLECTOR = java.util.stream.Collectors.mapping(INSTANCE::map, java.util.stream.Collectors.toList());

  @io.vertx.codegen.annotations.GenIgnore
  default MySQLDataObject map(io.vertx.sqlclient.Row row) {
    MySQLDataObject obj = new MySQLDataObject();
    Object val;
    int idx;
    if ((idx = row.getColumnIndex("duration")) != -1 && (val = row.get(java.time.Duration.class, idx)) != null) {
      obj.setDuration((java.time.Duration)val);
    }
    return obj;
  }
}

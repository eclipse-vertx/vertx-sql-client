package io.vertx.sqlclient.templates;

/**
 * Mapper for {@link MySQLDataObject}.
 * NOTE: This class has been automatically generated from the {@link MySQLDataObject} original class using Vert.x codegen.
 */
public class MySQLDataObjectRowMapper implements java.util.function.Function<io.vertx.sqlclient.Row, MySQLDataObject> {

  public static final java.util.function.Function<io.vertx.sqlclient.Row, MySQLDataObject> INSTANCE = new MySQLDataObjectRowMapper();

  public static final java.util.stream.Collector<io.vertx.sqlclient.Row, ?, java.util.List<MySQLDataObject>> COLLECTOR = java.util.stream.Collectors.mapping(INSTANCE, java.util.stream.Collectors.toList());

  public MySQLDataObject apply(io.vertx.sqlclient.Row row) {
    MySQLDataObject obj = new MySQLDataObject();
    Object val;
    int idx;
    if ((idx = row.getColumnIndex("duration")) != -1 && (val = row.get(java.time.Duration.class, idx)) != null) {
      obj.setDuration((java.time.Duration)val);
    }
    return obj;
  }
}

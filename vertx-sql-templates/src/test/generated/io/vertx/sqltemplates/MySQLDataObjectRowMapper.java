package io.vertx.sqltemplates;

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
    val = row.get(java.time.Duration.class, "duration");
    if (val != null) {
      obj.setDuration((java.time.Duration)val);
    }
    return obj;
  }
}

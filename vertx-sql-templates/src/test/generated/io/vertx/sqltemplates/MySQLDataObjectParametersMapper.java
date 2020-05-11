package io.vertx.sqltemplates;

/**
 * Mapper for {@link MySQLDataObject}.
 * NOTE: This class has been automatically generated from the {@link MySQLDataObject} original class using Vert.x codegen.
 */
public class MySQLDataObjectParametersMapper implements java.util.function.Function<MySQLDataObject, java.util.Map<String, Object>> {

  public static final java.util.function.Function<MySQLDataObject, java.util.Map<String, Object>> INSTANCE = new MySQLDataObjectParametersMapper();

  public java.util.Map<String, Object> apply(MySQLDataObject obj) {
    java.util.Map<String, Object> params = new java.util.HashMap<>();
    params.put("duration", obj.getDuration());
    return params;
  }
}

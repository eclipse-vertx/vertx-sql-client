package io.vertx.sqlclient.template;

/**
 * Mapper for {@link PostgreSQLDataObject}.
 * NOTE: This class has been automatically generated from the {@link PostgreSQLDataObject} original class using Vert.x codegen.
 */
public class PostgreSQLDataObjectParamMapper implements java.util.function.Function<PostgreSQLDataObject, java.util.Map<String, Object>> {

  public static final java.util.function.Function<PostgreSQLDataObject, java.util.Map<String, Object>> INSTANCE = new PostgreSQLDataObjectParamMapper();

  public java.util.Map<String, Object> apply(PostgreSQLDataObject obj) {
    java.util.Map<String, Object> params = new java.util.HashMap<>();
    params.put("box", obj.getBox());
    params.put("circle", obj.getCircle());
    params.put("interval", obj.getInterval());
    params.put("line", obj.getLine());
    params.put("lineSegment", obj.getLineSegment());
    params.put("path", obj.getPath());
    params.put("point", obj.getPoint());
    params.put("polygon", obj.getPolygon());
    return params;
  }
}

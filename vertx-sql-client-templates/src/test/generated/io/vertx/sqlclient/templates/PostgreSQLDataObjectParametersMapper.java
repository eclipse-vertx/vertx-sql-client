package io.vertx.sqlclient.templates;

/**
 * Mapper for {@link PostgreSQLDataObject}.
 * NOTE: This class has been automatically generated from the {@link PostgreSQLDataObject} original class using Vert.x codegen.
 */
@io.vertx.codegen.annotations.VertxGen
public interface PostgreSQLDataObjectParametersMapper extends io.vertx.sqlclient.templates.TupleMapper<PostgreSQLDataObject> {

  PostgreSQLDataObjectParametersMapper INSTANCE = new PostgreSQLDataObjectParametersMapper() {};

  default io.vertx.sqlclient.Tuple map(java.util.function.Function<Integer, String> mapping, int size, PostgreSQLDataObject params) {
    java.util.Map<String, Object> args = map(params);
    Object[] array = new Object[size];
    for (int i = 0;i < array.length;i++) {
      String column = mapping.apply(i);
      array[i] = args.get(column);
    }
    return io.vertx.sqlclient.Tuple.wrap(array);
  }

  default java.util.Map<String, Object> map(PostgreSQLDataObject obj) {
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

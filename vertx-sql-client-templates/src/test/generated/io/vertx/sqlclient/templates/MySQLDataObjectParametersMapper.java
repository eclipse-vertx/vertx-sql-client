package io.vertx.sqlclient.templates;

/**
 * Mapper for {@link MySQLDataObject}.
 * NOTE: This class has been automatically generated from the {@link MySQLDataObject} original class using Vert.x codegen.
 */
@io.vertx.codegen.annotations.VertxGen
public interface MySQLDataObjectParametersMapper extends io.vertx.sqlclient.templates.TupleMapper<MySQLDataObject> {

  MySQLDataObjectParametersMapper INSTANCE = new MySQLDataObjectParametersMapper() {};

  default io.vertx.sqlclient.Tuple map(java.util.function.Function<Integer, String> mapping, int size, MySQLDataObject params) {
    java.util.Map<String, Object> args = map(params);
    Object[] array = new Object[size];
    for (int i = 0;i < array.length;i++) {
      String column = mapping.apply(i);
      array[i] = args.get(column);
    }
    return io.vertx.sqlclient.Tuple.wrap(array);
  }

  default java.util.Map<String, Object> map(MySQLDataObject obj) {
    java.util.Map<String, Object> params = new java.util.HashMap<>();
    params.put("duration", obj.getDuration());
    return params;
  }
}

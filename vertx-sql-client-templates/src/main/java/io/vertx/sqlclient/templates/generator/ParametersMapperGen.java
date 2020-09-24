package io.vertx.sqlclient.templates.generator;

import io.vertx.codegen.DataObjectModel;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.type.AnnotationValueInfo;
import io.vertx.sqlclient.templates.TupleMapper;
import io.vertx.sqlclient.templates.annotations.ParametersMapped;
import io.vertx.sqlclient.templates.annotations.TemplateParameter;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ParametersMapperGen extends MapperGenBase {

  public ParametersMapperGen() {
    kinds = Collections.singleton("dataObject");
    name = "data_object_mappers";
  }

  @Override
  public Collection<Class<? extends Annotation>> annotations() {
    return Collections.singletonList(DataObject.class);
  }

  @Override
  protected void renderDeclaration(DataObjectModel model, PrintWriter writer) {
    writer.print("@io.vertx.codegen.annotations.VertxGen\n");
    writer.print("public interface " + genSimpleName(model) + " extends " + genFunctionExtends(model) + " {\n");
  }

  private String genFunctionExtends(DataObjectModel model) {
    return TupleMapper.class.getName() + "<" + model.getType().getSimpleName() + ">";
  }

  @Override
  protected String genSimpleName(DataObjectModel model) {
    return model.getType().getSimpleName() + "ParametersMapper";
  }

  @Override
  protected Optional<AnnotationValueInfo> getAnnotation(DataObjectModel model) {
    return model
      .getAnnotations()
      .stream().filter(ann -> ann.getName().equals(ParametersMapped.class.getName()))
      .findFirst();
  }


  @Override
  protected void renderMembers(String visibility, DataObjectModel model, PrintWriter writer) {
    genToParams(visibility, model, writer);
  }

  private void genToParams(String visibility, DataObjectModel model, PrintWriter writer) {
    writer.print("\n");
    writer.print("  " + genSimpleName(model) + " INSTANCE = new " + genSimpleName(model) + "() {};\n");
    writer.print("\n");
    writer.print("  default io.vertx.sqlclient.Tuple map(java.util.function.Function<Integer, String> mapping, int size, " + model.getType().getSimpleName() + " params) {\n");
    writer.print("    java.util.Map<String, Object> args = map(params);\n");
    writer.print("    Object[] array = new Object[size];\n");
    writer.print("    for (int i = 0;i < array.length;i++) {\n");
    writer.print("      String column = mapping.apply(i);\n");
    writer.print("      array[i] = args.get(column);\n");
    writer.print("    }\n");
    writer.print("    return io.vertx.sqlclient.Tuple.wrap(array);\n");
    writer.print("  }\n");
    writer.print("\n");
    writer.print("  default java.util.Map<String, Object> map(" + model.getType().getSimpleName() + " obj) {\n");
    writer.print("    java.util.Map<String, Object> params = new java.util.HashMap<>();\n");
    model
      .getPropertyMap()
      .values()
      .stream()
      .filter(prop -> PK.contains(prop.getKind()))
      .forEach(pi -> {
        String templateParamName = getMappingName(pi, TemplateParameter.class.getName());
        if (templateParamName != null) {
          writer.print("    params.put(\"" + templateParamName + "\", obj." + pi.getGetterMethod() + "());\n");
        }
      });
    writer.print("    return params;\n");
    writer.print("  }\n");
  }
}

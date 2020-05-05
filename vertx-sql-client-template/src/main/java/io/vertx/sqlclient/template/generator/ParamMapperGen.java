package io.vertx.sqlclient.template.generator;

import io.vertx.codegen.DataObjectModel;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.type.AnnotationValueInfo;
import io.vertx.sqlclient.template.annotations.ParamsMapped;
import io.vertx.sqlclient.template.annotations.RowMapped;
import io.vertx.sqlclient.template.annotations.TemplateParam;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ParamMapperGen extends MapperGenBase {

  public ParamMapperGen() {
    kinds = Collections.singleton("dataObject");
    name = "data_object_mappers";
  }

  @Override
  public Collection<Class<? extends Annotation>> annotations() {
    return Collections.singletonList(DataObject.class);
  }

  @Override
  protected String genFunctionExtends(DataObjectModel model) {
    return "java.util.function.Function<" + model.getType().getSimpleName() + ", java.util.Map<String, Object>>";
  }

  @Override
  protected String genSimpleName(DataObjectModel model) {
    return model.getType().getSimpleName() + "ParamMapper";
  }

  @Override
  protected Optional<AnnotationValueInfo> getAnnotation(DataObjectModel model) {
    return model
      .getAnnotations()
      .stream().filter(ann -> ann.getName().equals(ParamsMapped.class.getName()))
      .findFirst();
  }


  @Override
  protected void renderMembers(String visibility, DataObjectModel model, PrintWriter writer) {
    genToParams(visibility, model, writer);
  }

  private void genToParams(String visibility, DataObjectModel model, PrintWriter writer) {
    writer.print("\n");
    writer.print("  " + visibility + " static final java.util.function.Function<" + model.getType().getSimpleName() + ", java.util.Map<String, Object>> INSTANCE = new " + genSimpleName(model) + "();\n");
    writer.print("\n");
    writer.print("  " + visibility + " java.util.Map<String, Object> apply(" + model.getType().getSimpleName() + " obj) {\n");
    writer.print("    java.util.Map<String, Object> params = new java.util.HashMap<>();\n");
    model
      .getPropertyMap()
      .values()
      .stream()
      .filter(prop -> PK.contains(prop.getKind()))
      .forEach(pi -> {
        String templateParamName = getMappingName(pi, TemplateParam.class.getName());
        if (templateParamName != null) {
          writer.print("    params.put(\"" + templateParamName + "\", obj." + pi.getGetterMethod() + "());\n");
        }
      });
    writer.print("    return params;\n");
    writer.print("  }\n");
  }
}

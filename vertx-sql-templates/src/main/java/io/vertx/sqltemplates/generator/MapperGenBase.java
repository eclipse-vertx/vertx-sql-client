package io.vertx.sqltemplates.generator;

import io.vertx.codegen.Case;
import io.vertx.codegen.DataObjectModel;
import io.vertx.codegen.Generator;
import io.vertx.codegen.PropertyInfo;
import io.vertx.codegen.PropertyKind;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.type.AnnotationValueInfo;
import io.vertx.codegen.type.ClassTypeInfo;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class MapperGenBase extends Generator<DataObjectModel> {

  static final EnumSet<PropertyKind> PK = EnumSet.of(PropertyKind.VALUE, PropertyKind.LIST, PropertyKind.SET);

  public MapperGenBase() {
    kinds = Collections.singleton("dataObject");
    name = "data_object_mappers";
  }

  @Override
  public Collection<Class<? extends Annotation>> annotations() {
    return Collections.singletonList(DataObject.class);
  }

  @Override
  public String filename(DataObjectModel model) {
    if (model.isClass()) {
      return getAnnotation(model)
        .map(ann -> model.getType().getPackageName() + "." + genSimpleName(model) + ".java")
        .orElse(null);
    }
    return null;
  }

  protected abstract Optional<AnnotationValueInfo> getAnnotation(DataObjectModel model);

  private Case getCase(DataObjectModel model, String name) {
    AnnotationValueInfo abc = getAnnotation(model).get();
    ClassTypeInfo cti = (ClassTypeInfo) abc.getMember(name);
    switch (cti.getName()) {
      case "io.vertx.codegen.CamelCase":
        return Case.CAMEL;
      case "io.vertx.codegen.SnakeCase":
        return Case.SNAKE;
      case "io.vertx.codegen.LowerCamelCase":
        return Case.LOWER_CAMEL;
      default:
        throw new UnsupportedOperationException();
    }
  }

  private Case formatter;

  protected abstract String genSimpleName(DataObjectModel model);

  @Override
  public String render(DataObjectModel model, int index, int size, Map<String, Object> session) {
    StringWriter buffer = new StringWriter();
    PrintWriter writer = new PrintWriter(buffer);
    String visibility= model.isPublicConverter() ? "public" : "";

    formatter = getCase(model, "formatter");

    writer.print("package " + model.getType().getPackageName() + ";\n");
    writer.print("\n");
    writer.print("/**\n");
    writer.print(" * Mapper for {@link " + model.getType().getSimpleName() + "}.\n");
    writer.print(" * NOTE: This class has been automatically generated from the {@link " + model.getType().getSimpleName() + "} original class using Vert.x codegen.\n");
    writer.print(" */\n");
    writer.print("public class " + genSimpleName(model) + " implements " + genFunctionExtends(model) + " {\n");
    renderMembers(visibility, model, writer);
    writer.print("}\n");
    return buffer.toString();
  }

  protected abstract String genFunctionExtends(DataObjectModel model);

  protected abstract void renderMembers(String visibility, DataObjectModel model, PrintWriter writer);

  protected String getMappingName(PropertyInfo prop, String annotationName) {
    AnnotationValueInfo ann = prop.getAnnotation(annotationName);
    if (ann != null) {
      String value = (String) ann.getMember("name");
      if (value.length() > 0) {
        return value;
      }
    }
    String name = Character.toUpperCase(prop.getName().charAt(0)) + prop.getName().substring(1);
    return Case.CAMEL.to(formatter, name);
  }
}

package io.vertx.sqlclient.templates.generator;

import io.vertx.codegen.format.CamelCase;
import io.vertx.codegen.format.Case;
import io.vertx.codegen.DataObjectModel;
import io.vertx.codegen.Generator;
import io.vertx.codegen.PropertyInfo;
import io.vertx.codegen.PropertyKind;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.format.LowerCamelCase;
import io.vertx.codegen.format.SnakeCase;
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
      case "io.vertx.codegen.format.CamelCase":
        return CamelCase.INSTANCE;
      case "io.vertx.codegen.format.SnakeCase":
        return SnakeCase.INSTANCE;
      case "io.vertx.codegen.format.LowerCamelCase":
        return LowerCamelCase.INSTANCE;
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
    renderDeclaration(model, writer);
    renderMembers(visibility, model, writer);
    writer.print("}\n");
    return buffer.toString();
  }

  protected abstract void renderDeclaration(DataObjectModel model, PrintWriter writer);

  protected abstract void renderMembers(String visibility, DataObjectModel model, PrintWriter writer);

  protected String getMappingName(PropertyInfo prop, String annotationName) {
    AnnotationValueInfo ann = prop.getAnnotation(annotationName);
    if (ann != null) {
      String value = (String) ann.getMember("name");
      if (value.length() > 0) {
        return value;
      }
    }
    return LowerCamelCase.INSTANCE.to(formatter, prop.getName());
  }
}

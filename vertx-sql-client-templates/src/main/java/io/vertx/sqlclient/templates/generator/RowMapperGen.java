package io.vertx.sqlclient.templates.generator;

import io.vertx.codegen.DataObjectModel;
import io.vertx.codegen.MapperKind;
import io.vertx.codegen.PropertyInfo;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.type.AnnotationValueInfo;
import io.vertx.codegen.type.ClassKind;
import io.vertx.codegen.type.ClassTypeInfo;
import io.vertx.codegen.type.DataObjectInfo;
import io.vertx.codegen.type.MapperInfo;
import io.vertx.codegen.type.PrimitiveTypeInfo;
import io.vertx.codegen.type.TypeInfo;
import io.vertx.sqlclient.templates.RowMapper;
import io.vertx.sqlclient.templates.annotations.Column;
import io.vertx.sqlclient.templates.annotations.RowMapped;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class RowMapperGen extends MapperGenBase {

  public RowMapperGen() {
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

  @Override
  protected String genSimpleName(DataObjectModel model) {
    return model.getType().getSimpleName() + "RowMapper";
  }

  @Override
  protected Optional<AnnotationValueInfo> getAnnotation(DataObjectModel model) {
    return model
      .getAnnotations()
      .stream().filter(ann -> ann.getName().equals(RowMapped.class.getName()))
      .findFirst();
  }

  @Override
  protected void renderMembers(String visibility, DataObjectModel model, PrintWriter writer) {
    genFromRow(visibility, model, writer);
  }

  private String genFunctionExtends(DataObjectModel model) {
    return RowMapper.class.getName() + "<" + model.getType().getSimpleName() + ">";
  }

  private void genFromRow(String visibility, DataObjectModel model, PrintWriter writer) {
    writer.print("\n");
    writer.print("  " + genSimpleName(model) + " INSTANCE = new " + genSimpleName(model) + "() { };\n");
    writer.print("\n");
    writer.print("  @io.vertx.codegen.annotations.GenIgnore\n");
    writer.print("  java.util.stream.Collector<io.vertx.sqlclient.Row, ?, java.util.List<" + model.getType().getSimpleName() + ">> COLLECTOR = " + "java.util.stream.Collectors.mapping(INSTANCE::map, java.util.stream.Collectors.toList());\n");
    writer.print("\n");
    writer.print("  @io.vertx.codegen.annotations.GenIgnore\n");
    writer.print("  default " + model.getType().getSimpleName() + " map(io.vertx.sqlclient.Row row) {\n");
    writer.print("    " + model.getType().getSimpleName() + " obj = new " + model.getType().getSimpleName() + "();\n");
    writer.print("    Object val;\n");
    writer.print("    int idx;\n");
    genFromSingleValued(model, writer);
    writer.print("    return obj;\n");
    writer.print("  }\n");
  }

  private void genFromSingleValued(DataObjectModel model, PrintWriter writer) {
    model
      .getPropertyMap()
      .values()
      .stream()
      .filter(prop -> PK.contains(prop.getKind()))
      .filter(PropertyInfo::isSetter)
      .forEach(prop -> {
        String rowType = rowType(prop.getType());
        switch (prop.getKind()) {
          case VALUE: {
            Function<String, String> meth = getter_(prop.getType(), false);
            if (meth != null) {
              bilto4(writer, meth, prop, wrapExpr(prop.getType(), "(" + rowType + ")val"));
            }
            break;
          }
          case LIST: {
            Function<String, String> meth = getter_(prop.getType(), true);
            if (meth != null) {
              bilto4(writer, meth, prop, "java.util.Arrays.stream((" + rowType + "[])val).map(elt -> " + wrapExpr(prop.getType(), "elt") + ").collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new))");
            }
            break;
          }
          case SET: {
            Function<String, String> meth = getter_(prop.getType(), true);
            if (meth != null) {
              bilto4(writer, meth, prop, "java.util.Arrays.stream((" + rowType + "[])val).map(elt -> " + wrapExpr(prop.getType(), "elt") + ").collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new))");
            }
            break;
          }
        }
      });
    model
      .getPropertyMap()
      .values()
      .stream()
      .filter(prop -> PK.contains(prop.getKind()))
      .filter(prop -> prop.isAdder() && !prop.isSetter())
      .forEach(prop -> {
        Function<String, String> meth = getter_(prop.getType(), true);
        if (meth != null) {
          String columnName = getMappingName(prop, Column.class.getName());
          if (columnName != null) {
            String rowType = rowType(prop.getType());
            writer.print("    if ((idx = row.getColumnIndex(\"" + columnName + "\")) != -1 && (val = " + meth.apply("idx") + ") != null) {\n");
            writer.print("      for (" + rowType + " elt : (" + rowType + "[])val) {\n");
            writer.print("        obj." + prop.getAdderMethod() + "(" + wrapExpr(prop.getType(), "elt") + ");\n");
            writer.print("      }\n");
            writer.print("    }\n");
          }
        }
      });
  }

  private void bilto4(PrintWriter writer, Function<String, String> getter, PropertyInfo prop, String converter) {
    String columnName = getMappingName(prop, Column.class.getName());
    if (columnName != null) {
      writer.print("    if ((idx = row.getColumnIndex(\"" + columnName + "\")) != -1 && (val = " + getter.apply("idx") + ") != null) {\n");
      writer.print("      obj." + prop.getSetterMethod() + "(" + converter +  ");\n");
      writer.print("    }\n");
    }
  }

  private static String wrapExpr(TypeInfo type, String expr) {
    DataObjectInfo dataObject = type.getDataObject();
    if (dataObject != null) {
      MapperInfo deserializer = dataObject.getDeserializer();
      if (deserializer != null) {
        if (deserializer.getKind() == MapperKind.SELF) {
          return "new " + type.getName() + "(" + expr + ")";
        } else {
          return deserializer.getQualifiedName() + "." + String.join(".", deserializer
            .getSelectors()) + "(" + expr + ")";
        }
      }
      throw new UnsupportedOperationException();
    } else {
      return expr;
    }
  }

  private static String rowType(TypeInfo type) {
    DataObjectInfo dataObject = type.getDataObject();
    if (dataObject != null) {
      return rowType(dataObject.getJsonType());
    }
    return type.getName();
  }

  private static Function<String, String> getter_(TypeInfo type, boolean isArray) {
    String getter = getter(type);
    if (getter != null) {
      String name;
      String prefix;
      if (getter.startsWith("get")) {
        prefix = "get";
        name = getter.substring(3);
      } else if (getter.startsWith("is")) {
        prefix = "is";
        name = getter.substring(2);
      } else {
        throw new IllegalStateException();
      }
      return arg -> "row." + prefix + (isArray ? "ArrayOf" : "") + name + (isArray ? "s" : "") + "(" + arg + ")";
    }
    if (type.getKind() == ClassKind.ENUM || type instanceof ClassTypeInfo) {
      if (isArray) {
        return arg -> "row.get(" + type.getName() + "[].class, " + arg + ")";
      } else {
        return arg -> "row.get(" + type.getName() + ".class, " + arg + ")";
      }
    }
    return null;
  }

  private static String getter(TypeInfo type) {
    switch (type.getKind()) {
      case PRIMITIVE:
        PrimitiveTypeInfo pt = (PrimitiveTypeInfo) type;
        return getter(pt.getBoxed());
      case BOXED_PRIMITIVE:
        return "get" + type.getSimpleName();
      case STRING:
        return "getString";
      case JSON_OBJECT:
        return "getJsonObject";
      case JSON_ARRAY:
        return "getJsonArray";
    }
    if (type instanceof ClassTypeInfo) {
      DataObjectInfo dataObject = type.getDataObject();
      if (dataObject != null) {
        return getter(dataObject.getJsonType());
      }
      ClassTypeInfo ct = (ClassTypeInfo) type;
      switch (ct.getName()) {
        case "java.time.LocalDateTime":
          return "getLocalDateTime";
        case "java.time.LocalDate":
          return "getLocalDate";
        case "java.time.LocalTime":
          return "getLocalTime";
        case "java.time.OffsetTime":
          return "getOffsetTime";
        case "java.time.OffsetDateTime":
          return "getOffsetDateTime";
        case "java.time.temporal.Temporal":
          return "getTemporal";
        case "java.util.UUID":
          return "getUUID";
        case "io.vertx.core.buffer.Buffer":
          return "getBuffer";
      }
    }
    return null;
  }
}

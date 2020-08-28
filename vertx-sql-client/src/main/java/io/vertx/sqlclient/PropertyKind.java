package io.vertx.sqlclient;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;

import java.util.Objects;

/**
 * Represents which kind the property is.
 */
@VertxGen
public interface PropertyKind<T> {

  /**
   * @return a property kind matching the provided {@code name}, the {@code type} can be used to check
   *         the property value type or cast it to the expected type
   */
  static <T> PropertyKind<T> create(String name, Class<T> type) {
    Objects.requireNonNull(name, "No null name accepted");
    Objects.requireNonNull(type, "No null type accepted");
    return new PropertyKind<T>() {
      @Override
      public String name() {
        return name;
      }
      @Override
      public Class<T> type() {
        return type;
      }
      @Override
      public int hashCode() {
        return name.hashCode();
      }
      @Override
      public boolean equals(Object obj) {
        if (obj == this) {
          return true;
        } else if (obj instanceof PropertyKind) {
          return name.equals(((PropertyKind)obj).name());
        } else {
          return false;
        }
      }
      @Override
      public String toString() {
        return "PropertyKind[name=" + name + ",type=" + type.getName();
      }
    };
  }

  /**
   * @return the property name
   */
  String name();

  /**
   * @return the property type
   */
  @GenIgnore
  Class<T> type();
}

/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.sqlclient;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;

import java.util.Objects;

/**
 * The kind of the property, this can be used to fetch some specific property of the {@link SqlResult execution result}.
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

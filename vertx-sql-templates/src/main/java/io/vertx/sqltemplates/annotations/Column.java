package io.vertx.sqltemplates.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a Vert.x data object property for custom configuration when it is mapped to a column.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface Column {

  /**
   * @return the column name
   */
  String name() default "";

}

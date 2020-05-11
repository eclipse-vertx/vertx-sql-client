package io.vertx.sqltemplates.annotations;

import io.vertx.codegen.Case;
import io.vertx.codegen.LowerCamelCase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a Vert.x data object to generate an SQL template parameters mapper.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ParametersMapped {

  /**
   * @return the parameter name formatter, default maps to lower camel case.
   */
  Class<? extends Case> formatter() default LowerCamelCase.class;

}

package io.vertx.sqlclient.templates.annotations;

import io.vertx.codegen.format.Case;
import io.vertx.codegen.format.LowerCamelCase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a Vert.x data object to generate an SQL template row mapper.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface RowMapped {

  Class<? extends Case> formatter() default LowerCamelCase.class;

}

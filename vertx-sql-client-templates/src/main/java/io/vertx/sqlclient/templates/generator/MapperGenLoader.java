package io.vertx.sqlclient.templates.generator;

import io.vertx.codegen.processor.Generator;
import io.vertx.codegen.processor.GeneratorLoader;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.stream.Stream;

public class MapperGenLoader implements GeneratorLoader {

  @Override
  public Stream<Generator<?>> loadGenerators(ProcessingEnvironment processingEnv) {
    return Stream.of(new RowMapperGen(), new ParametersMapperGen());
  }
}

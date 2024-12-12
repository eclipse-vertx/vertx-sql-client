module io.vertx.sql.client.templates {
  requires io.vertx.sql.client;
  requires io.vertx.codegen.processor;
  requires io.vertx.codegen.api;
  requires io.vertx.core;
  requires java.compiler;
  requires static io.vertx.docgen;

  exports io.vertx.sqlclient.templates;
  exports io.vertx.sqlclient.templates.annotations;

}

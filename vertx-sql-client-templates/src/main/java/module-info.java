module io.vertx.sql.client.templates {

  requires static io.vertx.codegen.processor;
  requires static io.vertx.codegen.json;
  requires static io.vertx.codegen.api;
  requires static java.compiler;
  requires static io.vertx.docgen;

  requires io.vertx.sql.client;
  requires io.vertx.core;

  exports io.vertx.sqlclient.templates;
  exports io.vertx.sqlclient.templates.annotations;

  exports io.vertx.sqlclient.templates.impl to io.vertx.tests.sql.client.templates;

}

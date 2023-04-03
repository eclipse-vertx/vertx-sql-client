package io.vertx.sqlclient;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClientOptionsConverter;

@DataObject(generateConverter = true)
public class SqlCredentials {

  public String username;
  public String password;

  public SqlCredentials(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public SqlCredentials(JsonObject json) {
    SqlCredentialsConverter.fromJson(json, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    SqlCredentialsConverter.toJson(this, json);
    return json;
  }

}

package io.vertx.sqlclient.template;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

@DataObject
public class JsonObjectDataObject {

  private final JsonObject json;

  public JsonObjectDataObject(JsonObject json) {
    Objects.requireNonNull(json);
    this.json = json;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof JsonObjectDataObject) {
      JsonObjectDataObject that = (JsonObjectDataObject) obj;
      return json.equals(that.json);
    }
    return false;
  }

  public JsonObject toJson() {
    return json;
  }
}

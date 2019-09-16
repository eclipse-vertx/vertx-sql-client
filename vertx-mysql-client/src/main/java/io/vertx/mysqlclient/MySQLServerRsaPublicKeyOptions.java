package io.vertx.mysqlclient;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

/**
 * Key Options of server RSA public key used for encrypting password during authentication under insecure connections.
 */
@DataObject(generateConverter = true)
public class MySQLServerRsaPublicKeyOptions {
  private String keyPath;
  private Buffer buffer;

  public MySQLServerRsaPublicKeyOptions() {
  }

  public MySQLServerRsaPublicKeyOptions(JsonObject json) {
    MySQLServerRsaPublicKeyOptionsConverter.fromJson(json, this);
  }

  public MySQLServerRsaPublicKeyOptions(MySQLServerRsaPublicKeyOptions other) {
    this.keyPath = other.keyPath;
    this.buffer = other.buffer;
  }

  public Buffer getBuffer() {
    return buffer;
  }

  public String getKeyPath() {
    return keyPath;
  }

  public MySQLServerRsaPublicKeyOptions setBuffer(Buffer buffer) {
    this.buffer = buffer;
    return this;
  }

  public MySQLServerRsaPublicKeyOptions setKeyPath(String keyPath) {
    this.keyPath = keyPath;
    return this;
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    MySQLServerRsaPublicKeyOptionsConverter.toJson(this, json);
    return json;
  }
}

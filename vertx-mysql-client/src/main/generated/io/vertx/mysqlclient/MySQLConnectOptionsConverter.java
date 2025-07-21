package io.vertx.mysqlclient;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

/**
 * Converter and mapper for {@link io.vertx.mysqlclient.MySQLConnectOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.mysqlclient.MySQLConnectOptions} original class using Vert.x codegen.
 */
public class MySQLConnectOptionsConverter {

   static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, MySQLConnectOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "collation":
          if (member.getValue() instanceof String) {
            obj.setCollation((String)member.getValue());
          }
          break;
        case "charset":
          if (member.getValue() instanceof String) {
            obj.setCharset((String)member.getValue());
          }
          break;
        case "characterEncoding":
          if (member.getValue() instanceof String) {
            obj.setCharacterEncoding((String)member.getValue());
          }
          break;
        case "useAffectedRows":
          if (member.getValue() instanceof Boolean) {
            obj.setUseAffectedRows((Boolean)member.getValue());
          }
          break;
        case "sslMode":
          if (member.getValue() instanceof String) {
            obj.setSslMode(io.vertx.mysqlclient.SslMode.valueOf((String)member.getValue()));
          }
          break;
        case "authenticationPlugin":
          if (member.getValue() instanceof String) {
            obj.setAuthenticationPlugin(io.vertx.mysqlclient.MySQLAuthenticationPlugin.valueOf((String)member.getValue()));
          }
          break;
        case "serverRsaPublicKeyPath":
          if (member.getValue() instanceof String) {
            obj.setServerRsaPublicKeyPath((String)member.getValue());
          }
          break;
        case "serverRsaPublicKeyValue":
          if (member.getValue() instanceof String) {
            obj.setServerRsaPublicKeyValue(io.vertx.core.buffer.Buffer.fromJson((String)member.getValue()));
          }
          break;
        case "pipeliningLimit":
          if (member.getValue() instanceof Number) {
            obj.setPipeliningLimit(((Number)member.getValue()).intValue());
          }
          break;
      }
    }
  }

   static void toJson(MySQLConnectOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

   static void toJson(MySQLConnectOptions obj, java.util.Map<String, Object> json) {
    if (obj.getCollation() != null) {
      json.put("collation", obj.getCollation());
    }
    if (obj.getCharset() != null) {
      json.put("charset", obj.getCharset());
    }
    if (obj.getCharacterEncoding() != null) {
      json.put("characterEncoding", obj.getCharacterEncoding());
    }
    json.put("useAffectedRows", obj.isUseAffectedRows());
    if (obj.getSslMode() != null) {
      json.put("sslMode", obj.getSslMode().name());
    }
    if (obj.getAuthenticationPlugin() != null) {
      json.put("authenticationPlugin", obj.getAuthenticationPlugin().name());
    }
    if (obj.getServerRsaPublicKeyPath() != null) {
      json.put("serverRsaPublicKeyPath", obj.getServerRsaPublicKeyPath());
    }
    if (obj.getServerRsaPublicKeyValue() != null) {
      json.put("serverRsaPublicKeyValue", obj.getServerRsaPublicKeyValue().toJson());
    }
    json.put("pipeliningLimit", obj.getPipeliningLimit());
  }
}

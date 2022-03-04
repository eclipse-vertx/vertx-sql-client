/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.oracleclient.impl;

import io.vertx.core.VertxException;
import io.vertx.core.json.JsonObject;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static io.vertx.oracleclient.ServerMode.of;

public class OracleConnectionUriParser {

  private static final String SCHEME = "oracle:thin:";

  public static JsonObject parse(String connectionUri) {
    return parse(connectionUri, true);
  }

  public static JsonObject parse(String connectionUri, boolean exact) {
    if (connectionUri == null) {
      if (exact) {
        throw new NullPointerException("connectionUri is null");
      }
      return null;
    }
    if (!connectionUri.startsWith(SCHEME)) {
      if (exact) {
        throw new IllegalArgumentException("Invalid scheme: " + connectionUri);
      }
      return null;
    }
    JsonObject configuration = new JsonObject();
    RuntimeException caught;
    try {
      ParsingStage stage = ParsingStage.initial(connectionUri, configuration);
      do {
        stage = stage.doParse();
      } while (stage != null);
      return configuration;
    } catch (RuntimeException e) {
      caught = e;
    }
    IllegalArgumentException exception = new IllegalArgumentException("Cannot parse invalid connection URI: " + connectionUri);
    if (caught != null) {
      exception.initCause(caught);
    }
    throw exception;
  }

  private static abstract class ParsingStage {

    final String connectionUri;
    final int beginIdx;
    final JsonObject configuration;

    ParsingStage(String connectionUri, int beginIdx, JsonObject configuration) {
      this.connectionUri = connectionUri;
      this.beginIdx = beginIdx;
      this.configuration = configuration;
    }

    static ParsingStage initial(String connectionUri, JsonObject configuration) {
      return new UserAndPassword(connectionUri, SCHEME.length(), configuration);
    }

    abstract ParsingStage doParse();

    ParsingStage afterAtSign(int i) {
      if (i == connectionUri.length()) {
        throw new VertxException("Empty net location", true);
      }
      int j = connectionUri.indexOf("://", i);
      if (j >= i) {
        return new Protocol(connectionUri, i, j, configuration);
      }
      if (connectionUri.charAt(i) == '(') {
        throw new VertxException("TNS URL Format is not supported", true);
      }
      return hostOrIpV6(i);
    }

    ParsingStage hostOrIpV6(int i) {
      return connectionUri.charAt(i) == '[' ? new Ipv6(connectionUri, i + 1, configuration) : new Host(connectionUri, i, configuration);
    }

    ParsingStage afterHost(int i) {
      if (i == connectionUri.length()) {
        throw new VertxException("Missing service name or service id", true);
      }
      char c = connectionUri.charAt(i);
      if (c == ',') {
        throw new VertxException("URLs with multiple hosts are not supported yet", true);
      }
      if (c == '/') {
        return new ServiceName(connectionUri, i + 1, configuration);
      }
      if (c == ':') {
        return portOrServiceId(i + 1);
      }
      throw new VertxException("Invalid content after host", true);
    }

    ParsingStage portOrServiceId(int i) {
      int j = i;
      for (; j < connectionUri.length(); j++) {
        char c = connectionUri.charAt(j);
        if (c == ',' || c == ':' || c == '/' || c == '?') {
          break;
        }
        if (Character.getType(c) != Character.DECIMAL_DIGIT_NUMBER) {
          return new ServiceId(connectionUri, i, configuration);
        }
      }
      if (i == j) {
        throw new VertxException("Empty port or service id", true);
      }
      return new Port(connectionUri, i, j, configuration);
    }

    static class UserAndPassword extends ParsingStage {

      UserAndPassword(String connectionUri, int beginIdx, JsonObject configuration) {
        super(connectionUri, beginIdx, configuration);
      }

      @Override
      ParsingStage doParse() {
        int i = connectionUri.indexOf('@', beginIdx);
        if (i < beginIdx) {
          throw new VertxException("Did not find '@' sign", true);
        }
        if (i == beginIdx) {
          return afterAtSign(beginIdx + 1);
        }
        String userInfo = connectionUri.substring(beginIdx, i);
        String[] split = userInfo.split(userInfo.indexOf('/') >= 0 ? "/" : ":");
        if (split.length != 2) {
          throw new VertxException("User and password must be provided or omitted", true);
        }
        String user = split[0];
        if (user.isEmpty()) {
          throw new VertxException("User is missing", true);
        }
        String password = split[1];
        if (password.isEmpty()) {
          throw new VertxException("Password is missing", true);
        }
        configuration.put("user", decodeUrl(user));
        configuration.put("password", decodeUrl(password));

        return afterAtSign(i + 1);
      }
    }

    static class Protocol extends ParsingStage {

      final int endIdx;

      Protocol(String connectionUri, int beginIdx, int endIdx, JsonObject configuration) {
        super(connectionUri, beginIdx, configuration);
        this.endIdx = endIdx;
      }

      @Override
      ParsingStage doParse() {
        if (beginIdx == endIdx) {
          throw new VertxException("Empty protocol", true);
        }
        String protocol = connectionUri.substring(beginIdx, endIdx).toLowerCase(Locale.ROOT);
        if (protocol.equals("ldap") || protocol.equals("ldaps")) {
          throw new VertxException("LDAP Syntax is not supported", true);
        }
        if (protocol.equals("tcps")) {
          configuration.put("ssl", true);
        } else if (!protocol.equals("tcp")) {
          throw new VertxException("Unsupported protocol", true);
        }
        return hostOrIpV6(endIdx + 3);
      }
    }

    static class Ipv6 extends ParsingStage {

      Ipv6(String connectionUri, int beginIdx, JsonObject configuration) {
        super(connectionUri, beginIdx, configuration);
      }

      @Override
      ParsingStage doParse() {
        int i = connectionUri.indexOf(']', beginIdx);
        if (i < beginIdx) {
          throw new VertxException("Did not find ']' sign", true);
        }
        if (i == beginIdx) {
          throw new VertxException("Empty IPv6 address", true);
        }
        configuration.put("host", connectionUri.substring(beginIdx, i));
        return afterHost(i + 1);
      }
    }

    static class Host extends ParsingStage {

      Host(String connectionUri, int beginIdx, JsonObject configuration) {
        super(connectionUri, beginIdx, configuration);
      }

      @Override
      ParsingStage doParse() {
        int j = beginIdx;
        for (; j < connectionUri.length(); j++) {
          char c = connectionUri.charAt(j);
          if (c == ',' || c == ':' || c == '/' || c == '?') {
            break;
          }
        }
        if (beginIdx == j) {
          throw new VertxException("Empty host", true);
        }
        configuration.put("host", decodeUrl(connectionUri.substring(beginIdx, j)));
        return afterHost(j);
      }
    }

    static class Port extends ParsingStage {

      final int endIdx;

      Port(String connectionUri, int beginIdx, int endIdx, JsonObject configuration) {
        super(connectionUri, beginIdx, configuration);
        this.endIdx = endIdx;
      }

      @Override
      ParsingStage doParse() {
        long port = Long.parseLong(connectionUri.substring(beginIdx, endIdx));
        if (port > 65535 || port <= 0) {
          throw new VertxException("The port can only range in 1-65535", true);
        }
        configuration.put("port", port);
        if (endIdx == connectionUri.length()) {
          throw new VertxException("Missing service name or service id");
        }
        char c = connectionUri.charAt(endIdx);
        if (c == ',') {
          throw new VertxException("URLs with multiple hosts are not supported yet", true);
        }
        if (c == ':') {
          return new ServiceId(connectionUri, endIdx + 1, configuration);
        }
        if (c == '/') {
          return new ServiceName(connectionUri, endIdx + 1, configuration);
        }
        throw new IllegalStateException();
      }
    }

    static class ServiceId extends ParsingStage {

      ServiceId(String connectionUri, int beginIdx, JsonObject configuration) {
        super(connectionUri, beginIdx, configuration);
      }

      @Override
      ParsingStage doParse() {
        int i;
        if (beginIdx == connectionUri.length() || (i = connectionUri.indexOf('?', beginIdx)) == beginIdx) {
          throw new VertxException("Empty service id", true);
        }
        if (i >= 0) {
          configuration.put("serviceId", decodeUrl(connectionUri.substring(beginIdx, i)));
          return new ConnectionProps(connectionUri, i + 1, configuration);
        }
        configuration.put("serviceId", decodeUrl(connectionUri.substring(beginIdx)));
        return null;
      }
    }

    static class ServiceName extends ParsingStage {

      ServiceName(String connectionUri, int beginIdx, JsonObject configuration) {
        super(connectionUri, beginIdx, configuration);
      }

      @Override
      ParsingStage doParse() {
        int i = beginIdx;
        for (; i < connectionUri.length(); i++) {
          char c = connectionUri.charAt(i);
          if (c == ':' || c == '/' || c == '?') {
            break;
          }
        }
        if (beginIdx == i) {
          throw new VertxException("Empty service name", true);
        }
        configuration.put("serviceName", decodeUrl(connectionUri.substring(beginIdx, i)));
        if (i == connectionUri.length()) {
          return null;
        }
        char c = connectionUri.charAt(i);
        if (c == ':') {
          return new ServerMode(connectionUri, i + 1, configuration);
        }
        if (c == '/') {
          return new InstanceName(connectionUri, i + 1, configuration);
        }
        if (c == '?') {
          return new ConnectionProps(connectionUri, i + 1, configuration);
        }
        throw new IllegalStateException();
      }
    }

    static class ServerMode extends ParsingStage {

      ServerMode(String connectionUri, int beginIdx, JsonObject configuration) {
        super(connectionUri, beginIdx, configuration);
      }

      @Override
      ParsingStage doParse() {
        int i = beginIdx;
        for (; i < connectionUri.length(); i++) {
          char c = connectionUri.charAt(i);
          if (c == '/' || c == '?') {
            break;
          }
        }
        if (beginIdx == i) {
          throw new VertxException("Empty server mode", true);
        }
        io.vertx.oracleclient.ServerMode mode = of(decodeUrl(connectionUri.substring(beginIdx, i)));
        if (mode == null) {
          throw new VertxException("Invalid server mode", true);
        }
        configuration.put("serverMode", mode.toString());
        if (i == connectionUri.length()) {
          return null;
        }
        char c = connectionUri.charAt(i);
        if (c == '/') {
          return new InstanceName(connectionUri, i + 1, configuration);
        }
        if (c == '?') {
          return new ConnectionProps(connectionUri, i + 1, configuration);
        }
        throw new IllegalStateException();
      }
    }

    static class InstanceName extends ParsingStage {

      InstanceName(String connectionUri, int beginIdx, JsonObject configuration) {
        super(connectionUri, beginIdx, configuration);
      }

      @Override
      ParsingStage doParse() {
        if (beginIdx == connectionUri.length()) {
          throw new VertxException("Empty instance name", true);
        }
        int i = connectionUri.indexOf('?', beginIdx);
        if (i > 0) {
          configuration.put("instanceName", decodeUrl(connectionUri.substring(beginIdx, i)));
          return new ConnectionProps(connectionUri, i + 1, configuration);
        }
        configuration.put("instanceName", decodeUrl(connectionUri.substring(beginIdx)));
        return null;
      }
    }

    static class ConnectionProps extends ParsingStage {

      ConnectionProps(String connectionUri, int beginIdx, JsonObject configuration) {
        super(connectionUri, beginIdx, configuration);
      }

      @Override
      ParsingStage doParse() {
        if (beginIdx == connectionUri.length()) {
          throw new VertxException("Empty connection properties", true);
        }
        JsonObject properties = new JsonObject();
        for (String prop : connectionUri.substring(beginIdx).split("&")) {
          if (prop.isEmpty()) {
            throw new VertxException("Empty connection property", true);
          }
          String[] split = prop.split("=");
          if (split.length != 2) {
            throw new VertxException("Connection property without value: " + prop, true);
          }
          properties.put(decodeUrl(split[0]), decodeUrl(split[1]));
        }
        configuration.put("properties", properties);
        return null;
      }
    }
  }

  private static String decodeUrl(String url) {
    return URLDecoder.decode(url, StandardCharsets.UTF_8);
  }
}

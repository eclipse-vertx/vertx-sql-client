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
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.vertx.oracleclient.ServerMode.of;
import static oracle.jdbc.OracleConnection.CONNECTION_PROPERTY_TNS_ADMIN;

public class OracleConnectionUriParser {

  private static final String SCHEME = "oracle:thin:";

  public static JsonObject parse(final String connectionUri) {
    return parse(connectionUri, true);
  }

  public static JsonObject parse(final String connectionUri, final boolean exact) {
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
    final JsonObject configuration = new JsonObject();
    try {
      ParsingStage stage = ParsingStage.initial(connectionUri, configuration);
      do {
        stage = stage.doParse();
      } while (stage != null);

      if (configuration.getJsonArray("addresses") != null && configuration.getJsonArray("addresses").size() == 1) {
        // to stay simple, we remove the addresses array when only one pair host:port is provided
        configuration.remove("addresses");
      }

      return configuration;
    } catch (RuntimeException e) {
      throw new IllegalArgumentException("Cannot parse invalid connection URI: " + connectionUri, e);
    }
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
      int implicitProtocolIndex = connectionUri.indexOf("//", i);
      if (j >= i) {
        // explicit protocol
        return new Protocol(connectionUri, i, j, configuration);
      } else if (implicitProtocolIndex >= i) {
        // implicit tcp protocol
        return new Protocol(connectionUri, i, implicitProtocolIndex, configuration);
      }
      if (connectionUri.charAt(i) == '(') {
        return new TnsDescriptor(connectionUri, i, configuration);
      }
      if (configuration.containsKey("user")) {
        return hostOrIpV6(i);
      }
      j = connectionUri.lastIndexOf('?');
      if (j < i) j = connectionUri.length();
      for (int k = i; k < j; k++) {
        char c = connectionUri.charAt(k);
        if (c == ',' || c == '/' || c == ':') {
          return hostOrIpV6(i);
        }
      }
      return new TnsAlias(connectionUri, i, j, configuration);
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
        return hostOrIpV6(i + 1);
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
          // no "user/password"
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

    static class TnsAlias extends ParsingStage {

      final int endIdx;

      TnsAlias(String connectionUri, int beginIdx, int endIdx, JsonObject configuration) {
        super(connectionUri, beginIdx, configuration);
        this.endIdx = endIdx;
      }

      @Override
      ParsingStage doParse() {
        if (beginIdx == endIdx) {
          throw new VertxException("Empty TNS alias", true);
        }
        configuration.put("tnsAlias", decodeUrl(connectionUri.substring(beginIdx, endIdx)));
        return endIdx == connectionUri.length() ? null : new ConnectionProps(connectionUri, endIdx + 1, configuration);
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
          // empty protocol means "@//<host>..."
          return hostOrIpV6(endIdx + 2);
        }
        String protocol = connectionUri.substring(beginIdx, endIdx).toLowerCase(Locale.ROOT);
        if (protocol.equals("ldap") || protocol.equals("ldaps")) {
          throw new VertxException("LDAP Syntax is not supported", true);
        }
        if (protocol.equals("tcps")) {
          configuration.put("ssl", true);
        } else if (!protocol.equals("tcp")) {
          throw new VertxException("Unsupported protocol: " + protocol, true);
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
        final String host = connectionUri.substring(beginIdx, i);
        if(!configuration.containsKey("host")) {
          configuration.put("host", host);
        }
        JsonArray addresses = configuration.getJsonArray("addresses", new JsonArray());
        addresses.add(new JsonObject().put("host", host));
        configuration.put("addresses", addresses);
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
        final String host = decodeUrl(connectionUri.substring(beginIdx, j));
        if(!configuration.containsKey("host")) {
          configuration.put("host", host);
        }
        JsonArray addresses = configuration.getJsonArray("addresses", new JsonArray());
        addresses.add(new JsonObject().put("host", host));
        configuration.put("addresses", addresses);
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
        if(!configuration.containsKey("port")) {
          configuration.put("port", port);
        }
        JsonArray addresses = configuration.getJsonArray("addresses");
        JsonObject lastHostAndPort = addresses.getJsonObject(addresses.size() - 1);
        lastHostAndPort.put("port", (int) port);
        if (endIdx == connectionUri.length()) {
          throw new VertxException("Missing service name or service id");
        }
        char c = connectionUri.charAt(endIdx);
        if (c == ',') {
          return hostOrIpV6(endIdx + 1);
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
        configuration.put("serverMode", mode.toString().toUpperCase());
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
        // There may be already some properties in the case root level key/value pairs were found while parsing
        // with Oracle Net format.
        final JsonObject properties = configuration.getJsonObject("properties") != null ?
          configuration.getJsonObject("properties") : new JsonObject();
        for (String prop : connectionUri.substring(beginIdx).split("&")) {
          if (prop.isEmpty()) {
            throw new VertxException("Empty connection property", true);
          }
          String[] split = prop.split("=");
          if (split.length != 2) {
            throw new VertxException("Connection property without value: " + prop, true);
          }
          String key = decodeUrl(split[0]);
          String value = decodeUrl(split[1]);
          if (key.equalsIgnoreCase("TNS_ADMIN") || key.equalsIgnoreCase(CONNECTION_PROPERTY_TNS_ADMIN)) {
            configuration.put("tnsAdmin", value);
          } else {
            properties.put(key, value);
          }
        }
        if (!properties.isEmpty()) {
          configuration.put("properties", properties);
        }
        return null;
      }
    }
  }

  private static String decodeUrl(String url) {
    return URLDecoder.decode(url, StandardCharsets.UTF_8);
  }

  //--- TNS Descriptor parser -----------------------------------------------------
  static class TnsDescriptor extends ParsingStage {
    private static final Comparator<String> CASE_INSENSITIVE = String.CASE_INSENSITIVE_ORDER;

    TnsDescriptor(String connectionUri, int beginIdx, JsonObject configuration) {
      super(connectionUri, beginIdx, configuration);
    }

    @Override
    ParsingStage doParse() {
      final Node root = new DescriptorParser(connectionUri.substring(beginIdx)).parse();

      fillConfiguration(root);

      int i = connectionUri.indexOf('?', beginIdx);
      if (i > 0) {
        return new ConnectionProps(connectionUri, i + 1, configuration);
      }

      return null;
    }

    public static final class Address {
      private final String protocol;
      private final String hostname;
      private final Long port;
      private final Map<String, String> properties;

      private Address(String protocol, String hostname, Long port,
                      Map<String, String> properties) {
        this.protocol = protocol;
        this.hostname = hostname;
        this.port = port;
        this.properties = immutableCaseInsensitiveMap(properties);
      }

      public String getProtocol() {
        return protocol;
      }

      public String getHostname() {
        return hostname;
      }

      public String getHost() {
        return hostname;
      }

      public Long getPort() {
        return port;
      }

      /** Returns all scalar properties directly under this ADDRESS node. */
      public Map<String, String> getProperties() {
        return properties;
      }

      public String getProperty(String key) {
        Objects.requireNonNull(key, "key");
        return properties.get(key);
      }

      @Override
      public String toString() {
        return "Address{" +
          "protocol='" + protocol + '\'' +
          ", hostname='" + hostname + '\'' +
          ", port=" + port +
          '}';
      }
    }

    private static Long parsePort(String portText) {
      if (portText == null || portText.trim().isEmpty()) {
        return 1521L; // default
      }
      try {
        long port = Long.parseLong(portText.trim());
        if (port > 65535 || port <= 0) {
          throw new VertxException("The port can only range in 1-65535", true);
        }
        return port;
      } catch (NumberFormatException e) {
        throw new VertxException("The port is not a valid number: " + portText, true);
      }
    }

    private static void checkServerMode(final String server) {
      if (server != null) {
        switch (server.toLowerCase()) {
          case "dedicated":
          case "shared":
          case "pooled":
            break;
          default:
            throw new VertxException("The server mode is not valid: " + server + ", it must be either dedicated, shared, or pooled", true);
        }
      }
    }

    private void fillConfiguration(Node root) {
      final List<Node> addressNodes = new ArrayList<>();
      collectNodes(root, "ADDRESS", addressNodes);
      final List<Address> addresses = new ArrayList<>();
      for (Node addressNode : addressNodes) {
        Map<String, String> addressProperties = directScalarProperties(addressNode);
        String addressProtocol = addressProperties.get("PROTOCOL");
        String addressHost = firstNonBlank(addressProperties.get("HOST"),
          addressProperties.get("HOSTNAME"));
        Long addressPort = parsePort(addressProperties.get("PORT"));
        addresses.add(new Address(addressProtocol, addressHost, addressPort,
          addressProperties));
      }

      if (addresses.isEmpty()) {
        throw new IllegalArgumentException("TNS descriptor contains no ADDRESS entry");
      }

      final Map<String, List<String>> valuesByKey = new TreeMap<>(CASE_INSENSITIVE);
      collectScalarValues(root, valuesByKey);

      final Node connectData = findFirstNode(root, "CONNECT_DATA");
      Map<String, String> connectDataProperties = connectData == null
        ? new TreeMap<>(CASE_INSENSITIVE)
        : directScalarProperties(connectData);

      final Address firstAddress = addresses.get(0);
      final String serviceName = firstNonBlank(connectDataProperties.get("SERVICE_NAME"),
        firstValue(valuesByKey, "SERVICE_NAME"));
      final String sid = firstNonBlank(connectDataProperties.get("SID"),
        firstValue(valuesByKey, "SID"));
      final String instanceName = firstNonBlank(connectDataProperties.get("INSTANCE_NAME"),
        firstValue(valuesByKey, "INSTANCE_NAME"));
      final String server = firstNonBlank(connectDataProperties.get("SERVER"),
        firstValue(valuesByKey, "SERVER"));
      checkServerMode(server);

      if ("TCPS".equalsIgnoreCase(firstAddress.getProtocol())) {
        configuration.put("ssl", true);
      }
      if (firstAddress.getHost() != null) {
        configuration.put("host", firstAddress.getHost());
      }
      if (firstAddress.getPort() != null) {
        configuration.put("port", firstAddress.getPort());
      }

      // manage several addresses if needed
      if (addresses.size() > 1) {
        final JsonArray addressesJSONArray = new JsonArray();
        for (Address address : addresses) {
          final JsonObject addressJson = new JsonObject().put("host", address.getHost());
          if (address.getPort() != null) {
            addressJson.put("port", address.getPort());
          }
          addressesJSONArray.add(addressJson);
        }
        configuration.put("addresses", addressesJSONArray);
      }

      if (serviceName != null) {
        configuration.put("serviceName", serviceName);
      }
      if (sid != null) {
        configuration.put("serviceId", sid);
      }
      if (server != null) {
        configuration.put("serverMode", server.toUpperCase());
      }
      if (instanceName != null) {
        configuration.put("instanceName", instanceName);
      }

      // Add first level entries as properties
      final JsonObject properties = new JsonObject();
      final Map<String, String> rootScalarProperties = directScalarProperties(root);
      rootScalarProperties.keySet().forEach((key) -> {
        properties.put(key, rootScalarProperties.get(key));
      });

      if (!properties.isEmpty()) {
        configuration.put("properties", properties);
      }
    }

    private static void collectNodes(Node node, String name, List<Node> result) {
      if (name.equalsIgnoreCase(node.name)) {
        result.add(node);
      }
      for (Node child : node.children) {
        collectNodes(child, name, result);
      }
    }

    private static Node findFirstNode(Node node, String name) {
      if (name.equalsIgnoreCase(node.name)) {
        return node;
      }
      for (Node child : node.children) {
        Node result = findFirstNode(child, name);
        if (result != null) {
          return result;
        }
      }
      return null;
    }

    private static Map<String, String> directScalarProperties(Node node) {
      Map<String, String> result = new TreeMap<>(CASE_INSENSITIVE);
      for (Node child : node.children) {
        if (child.value != null) {
          result.put(child.name, child.value);
        }
      }
      return result;
    }

    private static void collectScalarValues(
      Node node, Map<String, List<String>> valuesByKey) {
      if (node.value != null) {
        addValue(valuesByKey, node.name, node.value);
      }
      for (Node child : node.children) {
        collectScalarValues(child, valuesByKey);
      }
    }

    private static void addValue(
      Map<String, List<String>> valuesByKey, String key, String value) {
      List<String> values = valuesByKey.computeIfAbsent(key, k -> new ArrayList<>());
      values.add(value);
    }

    private static String firstValue(Map<String, List<String>> valuesByKey, String key) {
      List<String> values = valuesByKey.get(key);
      return values == null || values.isEmpty() ? null : values.get(0);
    }

    private static String firstNonBlank(String first, String second) {
      return first != null && !first.trim().isEmpty() ? first : second;
    }

    private static Map<String, String> immutableCaseInsensitiveMap(Map<String, String> source) {
      Map<String, String> copy = new TreeMap<>(CASE_INSENSITIVE);
      copy.putAll(source);
      return Collections.unmodifiableMap(copy);
    }

  }

  private static final class Node {
    private final String name;
    private final String value;
    private final List<Node> children;

    private Node(final String name) {
      this(name, null, new ArrayList<>());
    }

    private Node(final String name, final String value, final List<Node> children) {
      this.name = name;
      this.value = value;
      this.children = children;
    }
  }

  /**
   * TNS descriptor parser.
   */
  private static final class DescriptorParser {
    private final String input;
    private int position;

    private DescriptorParser(final String input) {
      this.input = input;
    }

    private Node parse() {
      skipWhitespaces();
      final Node root = parseNode();
      skipWhitespaces();
      if (position != input.length()) {
        throw error("Unexpected text after descriptor");
      }
      return root;
    }

    private Node parseNode() {
      expectLeftParenthesis();
      skipWhitespaces();
      int keyStart = position;
      while (position < input.length() && input.charAt(position) != '=') {
        if (input.charAt(position) == ')') {
          throw error("Missing '=' after key");
        }
        position++;
      }
      if (position == input.length()) {
        throw error("Missing '=' after key");
      }

      String key = input.substring(keyStart, position).trim();
      if (key.isEmpty()) {
        throw error("Key must not be empty");
      }
      position++; // =
      skipWhitespaces();

      if (position < input.length() && input.charAt(position) == '(') {
        Node group = new Node(key);
        while (true) {
          skipWhitespaces();
          if (position >= input.length()) {
            throw error("Unclosed group for key " + key);
          }
          if (input.charAt(position) == ')') {
            position++;
            return group;
          }
          group.children.add(parseNode());
        }
      }

      final int valueStart = position;
      while (position < input.length() && input.charAt(position) != ')') {
        position++;
      }
      if (position == input.length()) {
        throw error("Unclosed value for key " + key);
      }
      String scalar = input.substring(valueStart, position).trim();
      position++; // )
      return new Node(key, scalar, new ArrayList<>());
    }

    private void expectLeftParenthesis() {
      if (position >= input.length() || input.charAt(position) != '(') {
        throw error("Expected '" + '(' + "'");
      }
      position++;
    }

    private void skipWhitespaces() {
      while (position < input.length()
        && Character.isWhitespace(input.charAt(position))) {
        position++;
      }
    }

    private VertxException error(final String message) {
      return new VertxException(message + " at character " + position, true);
    }
  }

}

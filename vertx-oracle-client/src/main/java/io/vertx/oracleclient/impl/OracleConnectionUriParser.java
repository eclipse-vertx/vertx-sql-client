/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.oracleclient.impl;

import io.vertx.core.json.JsonObject;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

public class OracleConnectionUriParser {

  private static final String SCHEME_DESIGNATOR_REGEX = "oracle:thin:"; // URI scheme designator
  private static final String USER_INFO_REGEX = "(?<userinfo>[a-zA-Z0-9\\-._~%!*&&[^/]]+/[a-zA-Z0-9\\-._~%!*&&[^/]]+)?"; // username and password
  private static final String NET_LOCATION_REGEX = "@(?<netloc>([0-9.]+|\\[[a-zA-Z0-9:]+]|[a-zA-Z0-9\\-._~%]+))";
  private static final String PORT_REGEX = ":(?<port>\\d+)"; // port
  private static final String SID = ":(?<sid>[a-zA-Z0-9\\-._~%!*]+)"; // sid name

  private static final Pattern SCHEME_DESIGNATOR_PATTERN = Pattern.compile("^" + SCHEME_DESIGNATOR_REGEX);
  private static final Pattern FULL_URI_PATTERN = Pattern.compile("^"
    + SCHEME_DESIGNATOR_REGEX
    + USER_INFO_REGEX
    + NET_LOCATION_REGEX
    + PORT_REGEX
    + SID
    + "$");

  public static JsonObject parse(String connectionUri) {
    return parse(connectionUri, true);
  }

  public static JsonObject parse(String connectionUri, boolean exact) {
    try {
      Matcher matcher = SCHEME_DESIGNATOR_PATTERN.matcher(connectionUri);
      if (matcher.find() || exact) {
        JsonObject configuration = new JsonObject();
        doParse(connectionUri, configuration);
        return configuration;
      } else {
        return null;
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("Cannot parse invalid connection URI: " + connectionUri, e);
    }
  }

  // execute the parsing process and store options in the configuration
  private static void doParse(String connectionUri, JsonObject configuration) {
    Matcher matcher = FULL_URI_PATTERN.matcher(connectionUri);

    if (matcher.matches()) {
      // parse the user and password
      parseUserAndPassword(matcher.group("userinfo"), configuration);

      // parse the IP address/host/unix domainSocket address
      parseNetLocation(matcher.group("netloc"), configuration);

      // parse the port
      parsePort(matcher.group("port"), configuration);

      // parse the sid name
      parseSID(matcher.group("sid"), configuration);

    } else {
      throw new IllegalArgumentException("Wrong syntax of connection URI");
    }
  }

  private static void parseUserAndPassword(String userInfo, JsonObject configuration) {
    if (userInfo == null || userInfo.isEmpty()) {
      return;
    }
    String[] split = userInfo.split("/");
    if (split.length != 2) {
      throw new IllegalArgumentException("User and password must be provided or omitted");
    }
    String user = split[0];
    if (user.isEmpty()) {
      throw new IllegalArgumentException("User is missing");
    }
    String password = split[1];
    if (password.isEmpty()) {
      throw new IllegalArgumentException("Password is missing");
    }
    configuration.put("user", decodeUrl(user));
    configuration.put("password", decodeUrl(password));
  }

  private static void parseNetLocation(String hostInfo, JsonObject configuration) {
    if (hostInfo == null || hostInfo.isEmpty()) {
      return;
    }
    parseNetLocationValue(decodeUrl(hostInfo), configuration);
  }

  private static void parsePort(String portInfo, JsonObject configuration) {
    if (portInfo == null || portInfo.isEmpty()) {
      return;
    }
    int port;
    try {
      port = parseInt(decodeUrl(portInfo));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("The port must be a valid integer");
    }
    if (port > 65535 || port <= 0) {
      throw new IllegalArgumentException("The port can only range in 1-65535");
    }
    configuration.put("port", port);
  }

  private static void parseSID(String sidInfo, JsonObject configuration) {
    if (sidInfo == null || sidInfo.isEmpty()) {
      return;
    }
    configuration.put("database", decodeUrl(sidInfo));
  }

  private static void parseNetLocationValue(String hostValue, JsonObject configuration) {
    if (isRegardedAsIpv6Address(hostValue)) {
      configuration.put("host", hostValue.substring(1, hostValue.length() - 1));
    } else {
      configuration.put("host", hostValue);
    }
  }

  private static boolean isRegardedAsIpv6Address(String hostAddress) {
    return hostAddress.startsWith("[") && hostAddress.endsWith("]");
  }

  private static String decodeUrl(String url) {
    return URLDecoder.decode(url, StandardCharsets.UTF_8);
  }
}

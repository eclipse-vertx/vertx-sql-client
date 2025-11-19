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

package io.vertx.mssqlclient.impl;

import io.vertx.core.json.JsonObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;

/**
 * This is a parser for parsing connection URIs of SQL Server.
 * The format is defined by the client in an idiomatic way: sqlserver://[user[:[password]]@]host[:port][/database][?attribute1=value1&attribute2=value2...
 */
public class MSSQLConnectionUriParser {

  private static final String SCHEME_DESIGNATOR_REGEX = "(sqlserver)://"; // URI scheme designator
  private static final String USER_INFO_REGEX = "((?<userinfo>[a-zA-Z0-9\\-._~%!*]+(:[a-zA-Z0-9\\-._~%!*^]*)?)@)?"; // user name and password
  private static final String NET_LOCATION_REGEX = "(?<netloc>[0-9.]+|\\[[a-zA-Z0-9:]+]|[a-zA-Z0-9\\-._~%]+)?"; // ip v4/v6 address, host, domain socket address
  private static final String PORT_REGEX = "(:(?<port>\\d+))?"; // port
  private static final String DATABASE_REGEX = "(/(?<database>[a-zA-Z0-9\\-._~%!*]+))?"; // database name
  private static final String ATTRIBUTES_REGEX = "(\\?(?<attributes>.*))?"; // attributes

  private static final Pattern SCHEME_DESIGNATOR_PATTERN = Pattern.compile("^" + SCHEME_DESIGNATOR_REGEX);
  private static final Pattern FULL_URI_PATTERN = Pattern.compile("^"
    + SCHEME_DESIGNATOR_REGEX
    + USER_INFO_REGEX
    + NET_LOCATION_REGEX
    + PORT_REGEX
    + DATABASE_REGEX
    + ATTRIBUTES_REGEX
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

      // parse the database name
      parseDatabaseName(matcher.group("database"), configuration);

      // parse the attributes
      parseAttributes(matcher.group("attributes"), configuration);

    } else {
      throw new IllegalArgumentException("Wrong syntax of connection URI");
    }
  }

  private static void parseUserAndPassword(String userInfo, JsonObject configuration) {
    if (userInfo == null || userInfo.isEmpty()) {
      return;
    }
    if (occurExactlyOnce(userInfo, ":")) {
      int index = userInfo.indexOf(":");
      String user = userInfo.substring(0, index);
      if (user.isEmpty()) {
        throw new IllegalArgumentException("Can not only specify the password without a concrete user");
      }
      String password = userInfo.substring(index + 1);
      configuration.put("user", decodeUrl(user));
      configuration.put("password", decodeUrl(password));
    } else if (!userInfo.contains(":")) {
      configuration.put("user", decodeUrl(userInfo));
    } else {
      throw new IllegalArgumentException("Can not use multiple delimiters to delimit user and password");
    }
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

  private static void parseDatabaseName(String databaseInfo, JsonObject configuration) {
    if (databaseInfo == null || databaseInfo.isEmpty()) {
      return;
    }
    configuration.put("database", decodeUrl(databaseInfo));
  }

  private static void parseAttributes(String attributesInfo, JsonObject configuration) {
    if (attributesInfo == null || attributesInfo.isEmpty()) {
      return;
    }
    Map<String, String> properties = new HashMap<>();
    for (String parameterPair : attributesInfo.split("&")) {
      if (parameterPair.isEmpty()) {
        continue;
      }
      int indexOfDelimiter = parameterPair.indexOf("=");
      if (indexOfDelimiter < 0) {
        throw new IllegalArgumentException(format("Missing delimiter '=' of parameters \"%s\" in the part \"%s\"", attributesInfo, parameterPair));
      } else {
        String key = parameterPair.substring(0, indexOfDelimiter).toLowerCase();
        String value = decodeUrl(parameterPair.substring(indexOfDelimiter + 1).trim());
        switch (key) {
          case "port":
            parsePort(value, configuration);
            break;
          case "host":
            parseNetLocationValue(value, configuration);
            break;
          case "socket":
            configuration.put("host", value);
            break;
          case "user":
            configuration.put("user", value);
            break;
          case "password":
            configuration.put("password", value);
            break;
          case "database":
            configuration.put("database", value);
            break;
          default:
            properties.put(key, value);
            break;
        }
      }
    }
    if (!properties.isEmpty()) {
      configuration.put("properties", properties);
    }
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
    try {
      return URLDecoder.decode(url, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException("The connection uri contains unknown characters that can not be resolved.");
    }
  }

  private static boolean occurExactlyOnce(String uri, String character) {
    return uri.contains(character) && uri.indexOf(character) == uri.lastIndexOf(character);
  }
}

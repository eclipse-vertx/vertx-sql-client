/*
 * Copyright (C) 2019,2020 IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertx.db2client.impl;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.vertx.core.json.JsonObject;

/**
 * This is a parser for parsing connection URIs of DB2.
 * @see <a href="https://www.ibm.com/support/knowledgecenter/SSEPGG_9.7.0/com.ibm.db2.luw.apdv.java.doc/src/tpc/imjcc_r0052342.html">DB2 official doc</a> 
 */
public class DB2ConnectionUriParser {
    // TODO @AGG implement URI parsing
  private static final String SCHEME_DESIGNATOR_REGEX = "(db2)://"; // URI scheme designator
  private static final String USER_INFO_REGEX = "((?<userinfo>[a-zA-Z0-9\\-._~%!]+(:[a-zA-Z0-9\\-._~%!]*)?)@)?"; // user name and password
  private static final String NET_LOCATION_REGEX = "(?<host>[0-9.]+|\\[[a-zA-Z0-9:]+]|[a-zA-Z0-9\\-._~%]+)"; // ip v4/v6 address or host name
  private static final String PORT_REGEX = "(:(?<port>\\d+))?"; // port
  private static final String SCHEMA_REGEX = "(/(?<schema>[a-zA-Z0-9\\-._~%!]+))?"; // schema name
  private static final String ATTRIBUTES_REGEX = "(\\?(?<attributes>.*))?"; // attributes

  private static final String FULL_URI_REGEX = "^" // regex start
    + SCHEME_DESIGNATOR_REGEX
    + USER_INFO_REGEX
    + NET_LOCATION_REGEX
    + PORT_REGEX
    + SCHEMA_REGEX
    + ATTRIBUTES_REGEX
    + "$"; // regex end

  public static JsonObject parse(String connectionUri) {
    // if we get any exception during the parsing, then we throw an IllegalArgumentException.
    try {
      JsonObject configuration = new JsonObject();
      doParse(connectionUri, configuration);
      return configuration;
    } catch (Exception e) {
      throw new IllegalArgumentException("Cannot parse invalid connection URI: " + connectionUri, e);
    }
  }

  // execute the parsing process and store options in the configuration
  private static void doParse(String connectionUri, JsonObject configuration) {
    Pattern pattern = Pattern.compile(FULL_URI_REGEX);
    Matcher matcher = pattern.matcher(connectionUri);

    if (matcher.matches()) {
      // parse the user and password
      parseUserAndPassword(matcher.group("userinfo"), configuration);

      // parse the IP address/hostname
      parseHost(matcher.group("host"), configuration);

      // parse the port
      parsePort(matcher.group("port"), configuration);

      // parse the schema name
      parseSchemaName(matcher.group("schema"), configuration);

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

  private static void parseHost(String hostInfo, JsonObject configuration) {
    if (hostInfo == null || hostInfo.isEmpty()) {
      return;
    }
    parseHostValue(decodeUrl(hostInfo), configuration);
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

  private static void parseSchemaName(String schemaInfo, JsonObject configuration) {
    if (schemaInfo == null || schemaInfo.isEmpty()) {
      return;
    }
    configuration.put("database", decodeUrl(schemaInfo));
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
          // Base Connection Parameters
          case "user":
            configuration.put("user", value);
            break;
          case "password":
            configuration.put("password", value);
            break;
          case "host":
            parseHostValue(value, configuration);
            break;
          case "port":
            parsePort(value, configuration);
            break;
          case "socket":
            configuration.put("socket", value);
            break;
          case "schema":
            configuration.put("database", value);
            break;
          default:
            configuration.put(key, value);
            break;
        }
      }
    }
    if (!properties.isEmpty()) {
      configuration.put("properties", properties);
    }
  }

  private static void parseHostValue(String hostValue, JsonObject configuration) {
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

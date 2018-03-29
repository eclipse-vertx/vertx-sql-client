package io.reactiverse.pgclient.impl;

import io.reactiverse.pgclient.PgConnectOptions;
import io.reactiverse.pgclient.PgPoolOptions;
import io.vertx.core.json.JsonObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.*;
import static java.lang.String.*;

/**
 * This is Parser for parsing connection URIs of PostgreSQL.
 * Based on Version 9.6
 *
 * @author Billy Yuan <billy112487983@gmail.com>
 */
public class PgConnectionUriParser {
  private static final String FULL_URI_REGEX = "^postgre(?:s|sql)://(?:(\\w+(?::\\S+)?)@)?([0-9.]+|\\[[A-Za-z0-9:]+]|[A-Za-z0-9.%\\-_]+)?(?::(\\d+))?(?:/([A-Za-z0-9_\\-]+))?(?:\\?(.*))?$";

  private static int USER_INFO_GROUP = 1;
  private static int NET_LOCATION_GROUP = 2;
  private static int PORT_GROUP = 3;
  private static int DATABASE_GROUP = 4;
  private static int PARAMETER_GROUP = 5;

  private final String connectionUri;

  private JsonObject configuration = new JsonObject();

  private PgConnectionUriParser(String connectionUri) {
    this.connectionUri = connectionUri;
    doParse();
  }

  private static JsonObject parse(String connectionUri) {
    // if we get any exception during the parsing, then we return a null.
    try {
      PgConnectionUriParser pgConnectionUriParser = new PgConnectionUriParser(connectionUri);
      return pgConnectionUriParser.getConfiguration();
    } catch (Exception e) {
      return null;
    }
  }

  public static PgPoolOptions translateToPgPoolOptions(String connectionUri) {
    JsonObject parsedConfiguration = parse(connectionUri);
    if (parsedConfiguration == null) {
      return new PgPoolOptions();
    } else {
      return new PgPoolOptions(parsedConfiguration);
    }
  }

  public static PgConnectOptions translateToPgConnectOptions(String connectionUri) {
    JsonObject parsedConfiguration = parse(connectionUri);
    if (parsedConfiguration == null) {
      return new PgConnectOptions();
    } else {
      return new PgConnectOptions(parsedConfiguration);
    }
  }

  private JsonObject getConfiguration() {
    return configuration;
  }

  public String getConnectionUri() {
    return connectionUri;
  }

  private void doParse() {
    Pattern pattern = Pattern.compile(FULL_URI_REGEX);
    Matcher matcher = pattern.matcher(connectionUri);

    if (matcher.matches()) {
      // parse the username and password
      parseUsernameAndPassword(matcher.group(USER_INFO_GROUP));

      // parse the IP address/host/unix domainSocket address
      parseNetLocation(matcher.group(NET_LOCATION_GROUP));

      // parse the port
      parsePort(matcher.group(PORT_GROUP));

      // parse the database name
      parseDatabaseName(matcher.group(DATABASE_GROUP));

      // parse the parameters
      parseParameters(matcher.group(PARAMETER_GROUP));

    } else {
      throw new IllegalArgumentException("Wrong syntax of connection URI");
    }
  }

  private void parseUsernameAndPassword(String userInfo) {
    if (userInfo == null || userInfo.isEmpty()) {
      return;
    }
    if (occurExactlyOnce(userInfo, ":")) {
      int index = userInfo.indexOf(":");
      String username = userInfo.substring(0, index);
      if (username.isEmpty()) {
        throw new IllegalArgumentException("Can not only specify the password without a concrete username");
      }
      String password = userInfo.substring(index + 1);
      configuration.put("username", decodeUrl(username));
      configuration.put("password", decodeUrl(password));
    } else if (!userInfo.contains(":")) {
      configuration.put("username", decodeUrl(userInfo));
    } else {
      throw new IllegalArgumentException("Can not use multiple delimiters to delimit username and password");
    }
  }

  private void parseNetLocation(String hostInfo) {
    if (hostInfo == null || hostInfo.isEmpty()) {
      return;
    }
    parseNetLocationValue(decodeUrl(hostInfo));
  }

  private void parsePort(String portInfo) {
    if (portInfo == null || portInfo.isEmpty()) {
      return;
    }
    int port;
    try {
      port = parseInt(decodeUrl(portInfo));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("The post must be a valid integer");
    }
    if (port > 65535 || port <= 0) {
      throw new IllegalArgumentException("The post can only range in 1-65535");
    }
    configuration.put("port", port);
  }

  private void parseDatabaseName(String databaseInfo) {
    if (databaseInfo == null || databaseInfo.isEmpty()) {
      return;
    }
    configuration.put("database", decodeUrl(databaseInfo));

  }

  private void parseParameters(String parametersInfo) {
    if (parametersInfo == null || parametersInfo.isEmpty()) {
      return;
    }
    for (String parameterPair : parametersInfo.split("&")) {
      if (parameterPair.isEmpty()) {
        continue;
      }
      int indexOfDelimiter = parameterPair.indexOf("=");
      if (indexOfDelimiter < 0) {
        throw new IllegalArgumentException(format("Missing delimiter '=' of parameters \"%s\" in the part \"%s\"", parametersInfo, parameterPair));
      } else {
        String key = parameterPair.substring(0, indexOfDelimiter).toLowerCase();
        String value = decodeUrl(parameterPair.substring(indexOfDelimiter + 1).trim());
        switch (key) {
          case "port":
            parsePort(value);
            break;
          case "host":
            parseNetLocationValue(value);
            break;
          case "hostaddr":
            configuration.put("host", value);
            break;
          case "user":
            configuration.put("username", value);
            break;
          case "password":
            configuration.put("password", value);
            break;
          case "dbname":
            configuration.put("database", value);
            break;
          default:
            configuration.put(key, value);
            break;
        }
      }
    }
  }

  private void parseNetLocationValue(String hostValue) {
    if (isRegardedAsDomainSocketAddress(hostValue)) {
      configuration.put("domainsocket", hostValue);
    } else if (isRegardedAsIpv6Address(hostValue)) {
      configuration.put("host", hostValue.substring(1, hostValue.length() - 1));
    } else {
      configuration.put("host", hostValue);
    }
  }

  private boolean isRegardedAsIpv6Address(String hostAddress) {
    return hostAddress.startsWith("[") && hostAddress.endsWith("]");
  }

  private boolean isRegardedAsDomainSocketAddress(String hostAddress) {
    return hostAddress.startsWith("/") || hostAddress.startsWith("%2F");
  }

  private String decodeUrl(String url) {
    try {
      return URLDecoder.decode(url, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException("The connection uri contains unknown characters that can not be resolved.");
    }
  }

  private boolean occurExactlyOnce(String uri, String character) {
    return uri.contains(character) && uri.indexOf(character) == uri.lastIndexOf(character);
  }
}

package io.vertx.sqlclient.utils;

/**
 * Testing utilities related to operating systems
 */
public class OperatingSystemUtils {
  private static final String osName = System.getProperty("os.name").toLowerCase();

  public static boolean isOsX() {
    return osName.contains("os x") || osName.contains("mac");
  }

  public static boolean isWindows() {
    return osName.contains("windows");
  }
}

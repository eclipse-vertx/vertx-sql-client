package io.vertx.clickhouse.clickhousenative;

public class ClickhouseConstants {
  public static final int DBMS_MIN_REVISION_WITH_TEMPORARY_TABLES = 50264;
  public static final int DBMS_MIN_REVISION_WITH_TOTAL_ROWS_IN_PROGRESS = 51554;
  public static final int DBMS_MIN_REVISION_WITH_BLOCK_INFO = 51903;

  public static final int DBMS_MIN_REVISION_WITH_CLIENT_INFO = 54032;
  public static final int DBMS_MIN_REVISION_WITH_SERVER_TIMEZONE = 54058;
  public static final int DBMS_MIN_REVISION_WITH_QUOTA_KEY_IN_CLIENT_INFO = 54060;
  public static final int DBMS_MIN_REVISION_WITH_SERVER_DISPLAY_NAME = 54372;
  public static final int DBMS_MIN_REVISION_WITH_VERSION_PATCH = 54401;
  public static final int DBMS_MIN_REVISION_WITH_SERVER_LOGS = 54406;
  public static final int DBMS_MIN_REVISION_WITH_COLUMN_DEFAULTS_METADATA = 54410;
  public static final int DBMS_MIN_REVISION_WITH_CLIENT_WRITE_INFO = 54420;
  public static final int DBMS_MIN_REVISION_WITH_SETTINGS_SERIALIZED_AS_STRINGS = 54429;
  public static final int DBMS_MIN_REVISION_WITH_INTERSERVER_SECRET = 54441;

  public static final int CLIENT_VERSION_MAJOR = 20;
  public static final int CLIENT_VERSION_MINOR = 10;
  public static final int CLIENT_VERSION_PATCH = 2;
  public static final int CLIENT_REVISION = 54441;

  public static final String OPTION_CLIENT_NAME = "CLIENT_NAME";
}

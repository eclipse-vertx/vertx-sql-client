package io.vertx.mssqlclient.impl.protocol.token;

public final class DataPacketStreamTokenType {
  public static final int ALTMETADATA_TOKEN = 0x88;
  public static final int ALTROW_TOKEN = 0xD3;
  public static final int COLMETADATA_TOKEN = 0x81;
  public static final int COLINFO_TOKEN = 0xA5;
  public static final int DONE_TOKEN = 0xFD;
  public static final int DONEPROC_TOKEN = 0xFE;
  public static final int DONEINPROC_TOKEN = 0xFF;
  public static final int ENVCHANGE_TOKEN = 0xE3;
  public static final int ERROR_TOKEN = 0xAA;
  public static final int FEATUREEXTACK = 0xAE;
  public static final int FEDAUTHINFO_TOKEN = 0xEE;
  public static final int INFO_TOKEN = 0xAB;
  public static final int LOGINACK_TOKEN = 0xAD;
  public static final int NBCROW_TOKEN = 0xD2;
  public static final int ORDER_TOKEN = 0xA9;
  public static final int RETURNSTATUS_TOKEN = 0x79;
  public static final int RETURNVALUE_TOKEN = 0xAC;
  public static final int ROW_TOKEN = 0xD1;
  public static final int SESSIONSTATE_TOKEN = 0xE4;
  public static final int SSPI_TOKEN = 0xED;
  public static final int TABNAME_TOKEN = 0xA4;

  public static final int OFFSET_TOKEN = 0x78;
}

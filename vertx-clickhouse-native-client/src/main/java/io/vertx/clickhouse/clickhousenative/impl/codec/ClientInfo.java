package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.clickhouse.clickhousenative.ClickhouseConstants;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;

public class ClientInfo {
  public static final int NO_QUERY = 0;
  public static final int INITIAL_QUERY = 1;

  private final ClickhouseNativeDatabaseMetadata meta;

  public ClientInfo(ClickhouseNativeDatabaseMetadata meta) {
    this.meta = meta;
  }

  public void serializeTo(ByteBuf buf) {
    int serverRevision = meta.getRevision();
    if (serverRevision < ClickhouseConstants.DBMS_MIN_REVISION_WITH_CLIENT_INFO) {
      throw new IllegalStateException(String.format("server revision %d < DBMS_MIN_REVISION_WITH_CLIENT_INFO(%d)",
        serverRevision, ClickhouseConstants.DBMS_MIN_REVISION_WITH_CLIENT_INFO));
    }
    buf.writeByte(INITIAL_QUERY);
    //initial_user
    ByteBufUtils.writePascalString("", buf);
    //initial_query_id
    ByteBufUtils.writePascalString("", buf);
    //initial_address
    ByteBufUtils.writePascalString("0.0.0.0:0", buf);
    //interface: TCP
    buf.writeByte(1);
    ByteBufUtils.writePascalString(System.getProperty("user.name"), buf);
    //TODO smagellan: fix client_hostname resolution
    ByteBufUtils.writePascalString("bhorse", buf);
    ByteBufUtils.writePascalString(meta.getClientName(), buf);
    ByteBufUtils.writeULeb128(ClickhouseConstants.CLIENT_VERSION_MAJOR, buf);
    ByteBufUtils.writeULeb128(ClickhouseConstants.CLIENT_VERSION_MINOR, buf);
    ByteBufUtils.writeULeb128(ClickhouseConstants.CLIENT_REVISION, buf);
    if (serverRevision >= ClickhouseConstants.DBMS_MIN_REVISION_WITH_QUOTA_KEY_IN_CLIENT_INFO) {
      //quota_key
      ByteBufUtils.writePascalString("", buf);
    }
    if (serverRevision >= ClickhouseConstants.DBMS_MIN_REVISION_WITH_VERSION_PATCH) {
      ByteBufUtils.writeULeb128(ClickhouseConstants.CLIENT_VERSION_PATCH, buf);
    }
  }
}

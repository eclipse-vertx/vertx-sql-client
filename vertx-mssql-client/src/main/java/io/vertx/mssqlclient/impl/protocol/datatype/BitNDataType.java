package io.vertx.mssqlclient.impl.protocol.datatype;

/**
 * BITNTYPE, the only valid lengths are 0x01 for non-null instances and 0x00 for NULL instances.
 */
public class BitNDataType extends MSSQLDataType {
  public static final BitNDataType BIT_1_DATA_TYPE = new BitNDataType(MSSQLDataTypeId.BITNTYPE_ID, Boolean.class, 1);

  private final int length;

  public BitNDataType(int id, Class<?> mappedJavaType, int length) {
    super(id, mappedJavaType);
    this.length = length;
  }

  public int length() {
    return length;
  }
}

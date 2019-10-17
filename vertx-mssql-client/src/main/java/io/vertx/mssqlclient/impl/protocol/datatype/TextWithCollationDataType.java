package io.vertx.mssqlclient.impl.protocol.datatype;

// BIGCHARTYPE, BIGVARCHRTYPE, TEXTTYPE, NTEXTTYPE, NCHARTYPE, or NVARCHARTYPE
public class TextWithCollationDataType extends MSSQLDataType {
  private final String collation;

  public TextWithCollationDataType(int id, Class<?> mappedJavaType, String collation) {
    super(id, mappedJavaType);
    this.collation = collation;
  }

  public String collation() {
    return collation;
  }
}

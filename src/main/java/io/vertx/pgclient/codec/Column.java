package io.vertx.pgclient.codec;

import java.util.Objects;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class Column {

  private final String name;
  private final int dataType;
  private final short dataFormat;
  private final int relationId;
  private final short relationAttributeNo;
  private final short length;
  private final int typeModifier;

  public Column(String name, int dataType, short dataFormat, short length, int relationId, short relationAttributeNo, int typeModifier) {
    this.name = name;
    this.dataType = dataType;
    this.dataFormat = dataFormat;
    this.length = length;
    this.relationId = relationId;
    this.relationAttributeNo = relationAttributeNo;
    this.typeModifier = typeModifier;
  }
  public String getName() {
    return name;
  }

  public int getDataType() {
    return dataType;
  }

  public short getDataFormat() {
    return dataFormat;
  }

  public int getRelationId() {
    return relationId;
  }

  public short getRelationAttributeNo() {
    return relationAttributeNo;
  }

  public short getLength() {
    return length;
  }

  public int getTypeModifier() {
    return typeModifier;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Column column = (Column) o;
    return dataType == column.dataType &&
      dataFormat == column.dataFormat &&
      relationId == column.relationId &&
      relationAttributeNo == column.relationAttributeNo &&
      length == column.length &&
      typeModifier == column.typeModifier &&
      Objects.equals(name, column.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, dataType, dataFormat, relationId, relationAttributeNo, length, typeModifier);
  }

  @Override
  public String toString() {
    return "Column{" +
      "name='" + name + '\'' +
      ", dataType=" + dataType +
      ", dataFormat=" + dataFormat +
      ", relationId=" + relationId +
      ", relationAttributeNo=" + relationAttributeNo +
      ", length=" + length +
      ", typeModifier=" + typeModifier +
      '}';
  }
}

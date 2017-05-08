package io.vertx.pgclient.codec.decoder;

import java.util.Objects;

public class Column {
  private final String name;
  private final ColumnType type;
  private final ColumnFormat format;
  private final int relationId;
  private final short relationAttributeNo;
  private final short length;
  private final int typeModifier;
  public Column(String name, ColumnType type, ColumnFormat format, short length, int relationId, short relationAttributeNo, int typeModifier) {
    this.name = name;
    this.type = type;
    this.format = format;
    this.length = length;
    this.relationId = relationId;
    this.relationAttributeNo = relationAttributeNo;
    this.typeModifier = typeModifier;
  }
  public String getName() {
    return name;
  }

  public ColumnType getType() {
    return type;
  }

  public ColumnFormat getFormat() {
    return format;
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
    return relationId == column.relationId &&
      relationAttributeNo == column.relationAttributeNo &&
      length == column.length &&
      typeModifier == column.typeModifier &&
      Objects.equals(name, column.name) &&
      type == column.type &&
      format == column.format;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, format, relationId, relationAttributeNo, length, typeModifier);
  }


  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Column{");
    sb.append("name='").append(name).append('\'');
    sb.append(", type=").append(type);
    sb.append(", format=").append(format);
    sb.append(", relationId=").append(relationId);
    sb.append(", relationAttributeNo=").append(relationAttributeNo);
    sb.append(", length=").append(length);
    sb.append(", typeModifier=").append(typeModifier);
    sb.append('}');
    return sb.toString();
  }
}

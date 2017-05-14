package io.vertx.pgclient.codec.decoder.message;

import java.util.Objects;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class Column {

  private final String name;
  private final int type;
  private final short format;
  private final int relationId;
  private final short relationAttributeNo;
  private final short length;
  private final int typeModifier;

  public Column(String name, int type, short format, short length, int relationId, short relationAttributeNo, int typeModifier) {
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

  public int getType() {
    return type;
  }

  public short getFormat() {
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
    return type == column.type &&
      format == column.format &&
      relationId == column.relationId &&
      relationAttributeNo == column.relationAttributeNo &&
      length == column.length &&
      typeModifier == column.typeModifier &&
      Objects.equals(name, column.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, format, relationId, relationAttributeNo, length, typeModifier);
  }

  @Override
  public String toString() {
    return "Column{" +
      "name='" + name + '\'' +
      ", type=" + type +
      ", format=" + format +
      ", relationId=" + relationId +
      ", relationAttributeNo=" + relationAttributeNo +
      ", length=" + length +
      ", typeModifier=" + typeModifier +
      '}';
  }
}

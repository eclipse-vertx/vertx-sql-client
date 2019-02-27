package io.reactiverse.mysqlclient2;

// this class is for verifying the use of Collector API
public class DummyObject {
  private int id;
  private short int2;
  private int int3;
  private int int4;
  private long int8;
  private float floatNum;
  private double doubleNum;
  private String varchar;

  public DummyObject(int id, short int2, int int3, int int4, long int8, float floatNum, double doubleNum, String varchar) {
    this.id = id;
    this.int2 = int2;
    this.int3 = int3;
    this.int4 = int4;
    this.int8 = int8;
    this.floatNum = floatNum;
    this.doubleNum = doubleNum;
    this.varchar = varchar;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public short getInt2() {
    return int2;
  }

  public void setInt2(short int2) {
    this.int2 = int2;
  }

  public int getInt3() {
    return int3;
  }

  public void setInt3(int int3) {
    this.int3 = int3;
  }

  public int getInt4() {
    return int4;
  }

  public void setInt4(int int4) {
    this.int4 = int4;
  }

  public long getInt8() {
    return int8;
  }

  public void setInt8(long int8) {
    this.int8 = int8;
  }

  public float getFloatNum() {
    return floatNum;
  }

  public void setFloatNum(float floatNum) {
    this.floatNum = floatNum;
  }

  public double getDoubleNum() {
    return doubleNum;
  }

  public void setDoubleNum(double doubleNum) {
    this.doubleNum = doubleNum;
  }

  public String getVarchar() {
    return varchar;
  }

  public void setVarchar(String varchar) {
    this.varchar = varchar;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DummyObject that = (DummyObject) o;

    if (id != that.id) return false;
    if (int2 != that.int2) return false;
    if (int3 != that.int3) return false;
    if (int4 != that.int4) return false;
    if (int8 != that.int8) return false;
    if (Float.compare(that.floatNum, floatNum) != 0) return false;
    if (Double.compare(that.doubleNum, doubleNum) != 0) return false;
    return varchar != null ? varchar.equals(that.varchar) : that.varchar == null;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = id;
    result = 31 * result + (int) int2;
    result = 31 * result + int3;
    result = 31 * result + int4;
    result = 31 * result + (int) (int8 ^ (int8 >>> 32));
    result = 31 * result + (floatNum != +0.0f ? Float.floatToIntBits(floatNum) : 0);
    temp = Double.doubleToLongBits(doubleNum);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + (varchar != null ? varchar.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "DummyObject{" +
      "id=" + id +
      ", int2=" + int2 +
      ", int3=" + int3 +
      ", int4=" + int4 +
      ", int8=" + int8 +
      ", floatNum=" + floatNum +
      ", doubleNum=" + doubleNum +
      ", varchar='" + varchar + '\'' +
      '}';
  }
}

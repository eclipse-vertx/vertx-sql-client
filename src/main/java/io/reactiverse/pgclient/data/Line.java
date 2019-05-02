package io.reactiverse.pgclient.data;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * Line data type in Postgres represented by the linear equation Ax + By + C = 0, where A and B are not both zero.
 */
@DataObject(generateConverter = true)
public class Line {
  private double a;
  private double b;
  private double c;

  public Line() {
    this(0.0, 0.0, 0.0);
  }

  public Line(double a, double b, double c) {
    this.a = a;
    this.b = b;
    this.c = c;
  }

  public Line(JsonObject json) {
    LineConverter.fromJson(json, this);
  }

  public double getA() {
    return a;
  }

  public void setA(double a) {
    this.a = a;
  }

  public double getB() {
    return b;
  }

  public void setB(double b) {
    this.b = b;
  }

  public double getC() {
    return c;
  }

  public void setC(double c) {
    this.c = c;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Line that = (Line) o;

    if (a != that.a) return false;
    if (b != that.b) return false;
    if (c != that.c) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    temp = Double.doubleToLongBits(a);
    result = (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(b);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(c);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "Line{" + a + "," + b + "," + c + "}";
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    LineConverter.toJson(this, json);
    return json;
  }
}

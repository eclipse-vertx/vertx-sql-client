package io.vertx.clickhouse.clickhousenative.impl;

public class Pair <K, V> {
  private final K left;
  private final V right;

  public Pair(K left, V right) {
    this.left = left;
    this.right = right;
  }

  public K getLeft() {
    return left;
  }

  public V getRight() {
    return right;
  }

  @Override
  public String toString() {
    return "Pair{" +
      "left=" + left +
      ", right=" + right +
      '}';
  }
}

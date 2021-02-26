package io.vertx.clickhouse.clickhousenative.impl;

import java.util.Objects;

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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Pair<?, ?> pair = (Pair<?, ?>) o;
    return Objects.equals(left, pair.left) && Objects.equals(right, pair.right);
  }

  @Override
  public int hashCode() {
    return Objects.hash(left, right);
  }

  @Override
  public String toString() {
    return "Pair{" +
      "left=" + left +
      ", right=" + right +
      '}';
  }
}

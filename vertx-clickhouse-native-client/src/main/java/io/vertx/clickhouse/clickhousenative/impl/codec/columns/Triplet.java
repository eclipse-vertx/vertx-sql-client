package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

public class Triplet<L, M, R> {
  private final L left;
  private final M middle;
  private final R right;

  public Triplet(L left, M middle, R right) {
    this.left = left;
    this.middle = middle;
    this.right = right;
  }

  public L left() {
    return left;
  }

  public M middle() {
    return middle;
  }

  public R right() {
    return right;
  }
}

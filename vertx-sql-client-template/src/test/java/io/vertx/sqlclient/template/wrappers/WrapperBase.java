package io.vertx.sqlclient.template.wrappers;

import java.util.Objects;

public abstract class WrapperBase<T> {

  private final T value;

  public WrapperBase(T value) {
    this.value = value;
  }

  public T get() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WrapperBase<T> that = (WrapperBase<T>) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }
}

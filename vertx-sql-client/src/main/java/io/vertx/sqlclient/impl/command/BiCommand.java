package io.vertx.sqlclient.impl.command;

import io.vertx.core.AsyncResult;

import java.util.function.Function;

public class BiCommand<T, R> extends CommandBase<R> {

  public final CommandBase<T> first;
  public final Function<T, AsyncResult<CommandBase<R>>> then;

  public BiCommand(CommandBase<T> first, Function<T, AsyncResult<CommandBase<R>>> then) {
    this.first = first;
    this.then = then;
  }
}

package io.vertx.sqlclient.impl.command;

import io.vertx.core.Future;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.future.PromiseInternal;

import java.util.ArrayList;
import java.util.List;

public class CompositeCommand extends CommandBase<Void> {

  private final List<CommandBase<?>> commands = new ArrayList<>();

  public <R> Future<R> add(ContextInternal context, CommandBase<R> cmd) {
    PromiseInternal<R> promise = context.promise();
    cmd.handler = promise;
    commands.add(cmd);
    return promise.future();
  }

  public List<CommandBase<?>> commands() {
    return commands;
  }
}

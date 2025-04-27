package io.vertx.sqlclient.internal.command;

import io.vertx.core.Completable;
import io.vertx.core.Future;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.PromiseInternal;

import java.util.ArrayList;
import java.util.List;

public class CompositeCommand extends CommandBase<Void> {

  private final List<CommandBase<?>> commands = new ArrayList<>();

  public <R> void add(CommandBase<R> cmd, Completable<R> handler) {
    cmd.handler = handler;
    commands.add(cmd);
  }

  public List<CommandBase<?>> commands() {
    return commands;
  }
}

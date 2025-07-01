package io.vertx.sqlclient.spi.protocol;

import io.vertx.core.Completable;

import java.util.ArrayList;
import java.util.List;

public class CompositeCommand extends CommandBase<Void> {

  private final List<CommandBase<?>> commands = new ArrayList<>();
  private final List<Completable<?>> handlers = new ArrayList<>();

  public <R> void add(CommandBase<R> cmd, Completable<R> handler) {
    commands.add(cmd);
    handlers.add(handler);
  }

  public List<CommandBase<?>> commands() {
    return commands;
  }

  public List<Completable<?>> handlers() {
    return handlers;
  }
}

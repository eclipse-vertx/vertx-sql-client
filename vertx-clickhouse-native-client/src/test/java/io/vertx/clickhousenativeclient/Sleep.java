package io.vertx.clickhousenativeclient;

public class Sleep {
  //updates may be async even for non-replicated tables;
  public static final int SLEEP_TIME = 100;

  public static void sleepOrThrow(int duration) {
    try {
      Thread.sleep(duration);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static void sleepOrThrow() {
    sleepOrThrow(SLEEP_TIME);
  }
}

package io.vertx.clickhousenativeclient.tck;

class Sleep {
  //updates may be async even for non-replicated tables;
  public static final int SLEEP_TIME = 100;

  static void sleepOrThrow() {
    try {
      Thread.sleep(SLEEP_TIME);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}

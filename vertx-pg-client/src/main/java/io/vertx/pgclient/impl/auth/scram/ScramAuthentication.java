package io.vertx.pgclient.impl.auth.scram;

import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.pgclient.ChannelBinding;

public class ScramAuthentication {

  private static final Logger logger = LoggerFactory.getLogger(ScramAuthentication.class);

  public static ScramAuthentication INSTANCE;

  static {
    ScramAuthentication instance;
    try {
      Class.forName("com.ongres.scram.client.ScramClient");
      instance = new ScramAuthentication();
    } catch (Throwable notFound) {
      logger.debug("SCRAM authentication is NOT available");
      instance = null;
    }
    INSTANCE = instance;
  }

  private ScramAuthentication() {
  }

  public ScramSession session(String username, char[] password, ChannelBinding channelBinding) {
    return new ScramSessionImpl(username, password, channelBinding);
  }
}

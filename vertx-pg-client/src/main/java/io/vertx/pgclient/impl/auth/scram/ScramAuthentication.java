package io.vertx.pgclient.impl.auth.scram;

import com.ongres.scram.client.ScramClient;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

public class ScramAuthentication {

  private static final Logger logger = LoggerFactory.getLogger(ScramAuthentication.class);

  public static ScramAuthentication INSTANCE;

  static {
    ScramAuthentication instance;
    try {
      ScramClient.MechanismsBuildStage builder = ScramClient.builder();
      logger.debug("Scram authentication is available " + builder);
      instance = new ScramAuthentication();
    } catch (Throwable notFound) {
      instance = null;
    }
    INSTANCE = instance;
  }

  private ScramAuthentication() {
  }

  public ScramSession session(String username, char[] password) {
    return new ScramSessionImpl(username, password);
  }
}

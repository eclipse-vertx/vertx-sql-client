package io.vertx.pgclient.codec.decoder.message.type;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class AuthenticationType {
  public static final int OK = 0;
  public static final int KERBEROS_V5 = 2;
  public static final int CLEARTEXT_PASSWORD = 3;
  public static final int MD5_PASSWORD = 5;
  public static final int SCM_CREDENTIAL = 6;
  public static final int GSS = 7;
  public static final int GSS_CONTINUE = 8;
  public static final int SSPI = 9;
}

package com.julienviet.pgclient.codec.decoder.message;

import com.julienviet.pgclient.codec.encoder.message.Execute;
import com.julienviet.pgclient.codec.Message;

/**
 *
 * <p>
 * The appearance of this message tells the frontend that another {@link Execute} should be issued against the
 * same portal to complete the operation. The {@link CommandComplete} message indicating completion of the source
 * SQL command is not sent until the portal's execution is completed
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class PortalSuspended implements Message {

  public static final PortalSuspended INSTANCE = new PortalSuspended();

  private PortalSuspended () {}

  @Override
  public String toString() {
    return "PortalSuspended{}";
  }
}

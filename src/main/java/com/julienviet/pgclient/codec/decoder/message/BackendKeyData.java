package com.julienviet.pgclient.codec.decoder.message;

import com.julienviet.pgclient.codec.Message;

import java.util.Objects;

/**
 *
 * <p>
 * This message provides secret-key data that the frontend must save if it wants to be able to issue cancel requests
 * later. The frontend should not respond to this message, but should continue listening
 * for a {@link ReadyForQuery} message.
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 *
 */

public class BackendKeyData implements Message {

  private final int processId;
  private final int secretKey;

  public BackendKeyData(int processId, int secretKey) {
    this.processId = processId;
    this.secretKey = secretKey;
  }

  public int getProcessId() {
    return processId;
  }

  public int getSecretKey() {
    return secretKey;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BackendKeyData that = (BackendKeyData) o;
    return processId == that.processId &&
      secretKey == that.secretKey;
  }

  @Override
  public int hashCode() {
    return Objects.hash(processId, secretKey);
  }


  @Override
  public String toString() {
    return "BackendKeyData{" +
      "processId=" + processId +
      ", secretKey=" + secretKey +
      '}';
  }
}

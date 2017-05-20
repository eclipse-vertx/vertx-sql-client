package com.julienviet.pgclient.codec.decoder.message;

import com.julienviet.pgclient.codec.Message;

import java.util.Objects;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class NotificationResponse implements Message {

  private final int processId;
  private final String channel;
  private final String payload;

  public NotificationResponse(int processId, String channel, String payload) {
    this.processId = processId;
    this.channel = channel;
    this.payload = payload;
  }

  public int getProcessId() {
    return processId;
  }

  public String getChannel() {
    return channel;
  }

  public String getPayload() {
    return payload;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    NotificationResponse that = (NotificationResponse) o;
    return processId == that.processId &&
      Objects.equals(channel, that.channel) &&
      Objects.equals(payload, that.payload);
  }

  @Override
  public int hashCode() {
    return Objects.hash(processId, channel, payload);
  }

  @Override
  public String toString() {
    return "NotificationResponse{" +
      "processId=" + processId +
      ", channel='" + channel + '\'' +
      ", payload='" + payload + '\'' +
      '}';
  }
}

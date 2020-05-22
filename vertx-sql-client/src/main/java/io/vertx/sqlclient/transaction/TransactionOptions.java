package io.vertx.sqlclient.transaction;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * Transaction options which could be used to control the characteristics at the start of transaction.
 */
@DataObject(generateConverter = true)
public class TransactionOptions {

  public static final TransactionOptions DEFAULT_TX_OPTIONS = new TransactionOptions();

  private TransactionIsolationLevel isolationLevel;
  private TransactionAccessMode accessMode;

  public TransactionOptions() {
  }

  public TransactionOptions(JsonObject json) {
    TransactionOptionsConverter.fromJson(json, this);
  }

  public TransactionOptions(TransactionIsolationLevel isolationLevel, TransactionAccessMode accessMode) {
    this.isolationLevel = isolationLevel;
    this.accessMode = accessMode;
  }

  public TransactionOptions(TransactionOptions other) {
    this.isolationLevel = other.isolationLevel;
    this.accessMode = other.accessMode;
  }

  /**
   * Set the {@link TransactionAccessMode transaction access mode} in the options.
   *
   * @param accessMode the access mode
   * @return a reference to this, so the API can be used fluently
   */
  public TransactionOptions setAccessMode(TransactionAccessMode accessMode) {
    this.accessMode = accessMode;
    return this;
  }

  /**
   * Get the {@link TransactionAccessMode transaction access mode} in the options.
   *
   * @return the transaction access mode
   */
  public TransactionAccessMode getAccessMode() {
    return accessMode;
  }

  /**
   * Set the {@link TransactionIsolationLevel transaction isolation level} in the options.
   *
   * @param isolationLevel the isolation level to specify
   * @return a reference to this, so the API can be used fluently
   */
  public TransactionOptions setIsolationLevel(TransactionIsolationLevel isolationLevel) {
    this.isolationLevel = isolationLevel;
    return this;
  }

  /**
   * Get the {@link TransactionIsolationLevel transaction isolation level} in the options.
   *
   * @return the isolation level
   */
  public TransactionIsolationLevel getIsolationLevel() {
    return isolationLevel;
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    TransactionOptionsConverter.toJson(this, json);
    return json;
  }
}

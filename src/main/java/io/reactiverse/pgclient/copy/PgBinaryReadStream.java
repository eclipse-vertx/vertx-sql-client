package io.reactiverse.pgclient.copy;

public interface PgBinaryReadStream extends PgCopyReadStream<CopyTuple> {

  @Override
  default CopyFormat getFormat() {
    return CopyFormat.BINARY;
  }
}

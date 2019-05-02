## Client Benchmarking

### Running the query benchmark

Runs query operations with 8 threads

```
> mvn clean package -Pbenchmark
> java -jar target/vertx-pg-client-0.3.1-SNAPSHOT-benchmark.jar
```

You can profile the benchmark

```
> java -jar target/vertx-pg-client-0.3.1-SNAPSHOT-benchmark.jar SingleSelectBenchmark.poolPreparedQuery -jvmArgsAppend "-XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:StartFlightRecording=duration=60s,filename=./profiling-data.jfr,name=profile,settings=profile"
```

just make sure to run one benchmark at a time.

## Client performance

### Outbound message size estimation

Some messages are really small, and it would be good allocate the right size instead of 256 by default.

### Row decoding

Investigate plugability of row decoder that can operate on `@DataObject` directly.

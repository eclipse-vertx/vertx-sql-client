## Client Benchmarking

### Running the query benchmark

Runs query operations with 8 threads

```
> mvn clean package
> java -jar target/vertx-pg-client-0.3.1-SNAPSHOT-benchmarks.jar
```

You can profile the benchmark

```
> java -jar target/vertx-pg-client-0.3.1-SNAPSHOT-benchmarks.jar SingleSelectBenchmark.poolPreparedQuery -jvmArgsAppend "-XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:StartFlightRecording=duration=60s,filename=./profiling-data.jfr,name=profile,settings=profile"
```

just make sure to run one benchmark at a time.

## Client performance

### Prepared statements UUID creation

UUID creation can be a bottleneck when creating lot of prepared statements, visible in benchmark.

This does not happen with the `PgOperations` interface that avoid to create `PgPreparedStament`.

### Extended queries and extended update execution

Rework extended queries commands, right now:

- initially Parse/Describe/Bind/Execute/Sync
- then Describe/Bind/Execute/Sync over and over again

instead we should do:

- initially Parse/Describe/Sync
- cache the result and then Bind/Execute/Sync

### Parameter binary encoding

Just encode them in binary it's more efficient and we don't need intermediate strings.

### Row decoding

Investigate plugability of row decoder that can operate on `@DataObject` directly. 

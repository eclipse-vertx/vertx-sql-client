## Client Benchmarking

### Running the query benchmark

Runs query operations with 8 threads

```
> mvn clean package
> java -jar target/vertx-pg-client-0.3.1-SNAPSHOT-benchmarks.jar
```

You can profile the benchmark

```
> java -jar target/vertx-pg-client-0.3.1-SNAPSHOT-benchmarks.jar -jvmArgsAppend "-XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:StartFlightRecording=duration=60s,filename=./profiling-data.jfr,name=profile,settings=profile"
```

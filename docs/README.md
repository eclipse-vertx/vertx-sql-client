
# The Reactive Postgres Client

* Simple API focusing on scalability and low overhead.
* Reactive and non blocking which able to handle many database connections with a single thread.
* Ranked 1 in the [TechEmpower Benchmark Round 15](https://www.techempower.com/benchmarks/#section=data-r15&hw=ph&test=db) _Single query_ benchmark.
* Top ranked in the TechEmpower Benchmark Round 16
   * Single query benchmark: [Ranked 1 physical](https://www.techempower.com/benchmarks/#section=data-r16&hw=ph&test=db) / [Ranked 1 cloud](https://www.techempower.com/benchmarks/#section=data-r16&hw=cl&test=db)
   * Multiples queries benchmark: [Ranked 1 physical](https://www.techempower.com/benchmarks/#section=data-r16&hw=ph&test=query) / [Ranked 1 cloud](https://www.techempower.com/benchmarks/#section=data-r16&hw=cl&test=query)
   * Data updates: [Ranked 1 physical](https://www.techempower.com/benchmarks/#section=data-r16&hw=ph&test=update) / [Ranked 1 cloud](https://www.techempower.com/benchmarks/#section=data-r16&hw=cl&test=update)
   * Fortunes: [Ranked 3 physical](https://www.techempower.com/benchmarks/#section=data-r16&hw=ph&test=fortune) / [Ranked 2 cloud](https://www.techempower.com/benchmarks/#section=data-r16&hw=cl&test=fortune)

## Features

* Event driven
* Lightweight
* Built-in connection pooling
* Prepared queries caching
* Publish / subscribe using Postgres `NOTIFY/LISTEN`
* Batch and cursor
* Row streaming
* `java.util.stream.Collector` row set transformation
* Command pipeling
* RxJava 1 and RxJava 2
* Direct memory to object without unnecessary copies
* Java 8 Date and Time
* SSL/TLS
* Unix domain socket
* HTTP/1.x CONNECT, SOCKS4a or SOCKS5 proxy support

## Documentation

* [Java](guide/java/index.md)
* [Kotlin](guide/kotlin/index.md)
* [Groovy](guide/groovy/index.md)
* [JavaScript](guide/js/index.md)
* [Ruby](guide/ruby/index.md)

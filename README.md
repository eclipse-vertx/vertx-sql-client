# Reactive Postgres Client

[![Build Status](https://travis-ci.org/vietj/reactive-pg-client.svg?branch=master)](https://travis-ci.org/vietj/reactive-pg-client)

The Reactive Postgres Client is a client for Postgres with a straightforward API focusing on scalability and low overhead.

The client is reactive and non blocking, allowing to handle many database connections with a single thread.

## Features

- Event driven
- Lightweight
- Built-in connection pooling
- Prepared queries caching
- Publish / subscribe using Postgres `NOTIFY/LISTEN`
- Batch and cursor support
- Row streaming
- Command pipeling
- RxJava 1 and RxJava 2 support
- Direct memory to object without unnecessary copies
- Java 8 Date and Time support
- SSL/TLS support
- HTTP/1.x CONNECT, SOCKS4a or SOCKS5 proxy support

## Using Postgres Client

To use the client, add the following dependency to the _dependencies_ section of your build descriptor:

* Maven (in your `pom.xml`):

```xml
<dependency>
  <groupId>com.julienviet</groupId>
  <artifactId>reactive-pg-client</artifactId>
  <version>0.5.0</version>
</dependency>
```

* Gradle (in your `build.gradle` file):

```groovy
dependencies {
  compile 'com.julienviet:reactive-pg-client:0.5.0'
}
```

Then the code is quite straightforward:

```java
// Pool options
PgPoolOptions options = new PgPoolOptions()
  .setPort(5432)
  .setHost("the-host")
  .setDatabase("the-db")
  .setUsername("user")
  .setPassword("secret")
  .setMaxSize(5);

// Create the client pool
PgPool client = PgClient.pool(options);

// A simple query
client.query("SELECT * FROM users WHERE id='julien'", ar -> {
  if (ar.succeeded()) {
    PgResult<Row> result = ar.result();
    System.out.println("Got " + result.size() + " results ");
  } else {
    System.out.println("Failure: " + ar.cause().getMessage());
  }

  // Now close the pool
  client.close();
});
```

## Web-site docs

* [Java docs](http://www.julienviet.com/reactive-pg-client/guide/java/index.html)
* [Javadoc](https://www.julienviet.com/reactive-pg-client/apidocs/index.html)
* [Kotlin docs](http://www.julienviet.com/reactive-pg-client/guide/kotlin/index.html)
* [Groovy docs](http://www.julienviet.com/reactive-pg-client/guide/groovy/index.html)
* [Ruby docs](http://www.julienviet.com/reactive-pg-client/guide/ruby/index.html)
* [JavaScript docs](http://www.julienviet.com/reactive-pg-client/guide/js/index.html)

## Snapshots

Snapshots are deploy in Sonatype OSS repository: https://oss.sonatype.org/content/repositories/snapshots/com/julienviet/reactive-pg-client/

## License

Apache License - Version 2.0

## Publishing docs

* mvn package -Pdocs
* cp -r target/docs docs/
* mv docs/reactive-pg-client docs/guide

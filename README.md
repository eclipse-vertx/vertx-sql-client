# High performance reactive Postgres Client

[![Build Status](https://travis-ci.org/vietj/reactive-pg-client.svg?branch=master)](https://travis-ci.org/vietj/reactive-pg-client)

## Why ?

_PgClient_ provides a simple API close to Postgres while focusing on scalability and low overhead.

It is reactive and non blocking, allowing to handle many database connections with a single thread.

## Features

- lightweight
- prepared queries caching
- publish / subscribe using Postgres `NOTIFY/LISTEN`
- batch support
- cursor streaming
- command pipeling

## Using Postgres Client

To use the client, add the following dependency to the _dependencies_ section of your build descriptor:

* Maven (in your `pom.xml`):

```xml
<dependency>
  <groupId>com.julienviet</groupId>
  <artifactId>reactive-pg-client</artifactId>
  <version>0.4.0</version>
</dependency>
```

* Gradle (in your `build.gradle` file):

```groovy
dependencies {
  compile 'com.julienviet:reactive-pg-client:0.4.0'
}
```

Then the code is quite straightforward:

```java
PgPoolOptions options = new PgPoolOptions()
  .setPort(5432)
  .setHost("the-host")
  .setDatabase("the-db")
  .setUsername("user")
  .setPassword("secret")
  .setMaxSize(5);

// Create the pool
PgPool pool = PgPool.pool(options);

// A simple query
pool.query("SELECT * FROM users WHERE id='julien'", ar -> {
  if (ar.succeeded()) {
    PgResult<Row> result = ar.result();
    System.out.println("Got " + result.size() + " results ");
  } else {
    System.out.println("Failure: " + ar.cause().getMessage());
  }

  // Close now the pool
  pool.close();
});
```

## Web-site docs

* [Java docs](http://www.julienviet.com/reactive-pg-client/guide/java/index.html)
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

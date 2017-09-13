# Async Postgres client for Eclipse Vert.x

[![Build Status](https://travis-ci.org/vietj/vertx-pg-client.svg?branch=master)](https://travis-ci.org/vietj/vertx-pg-client)

## What for ?

[Vert.x 3.5.0.Beta1](http://vertx.io)

## Using Postgres Client

To use the client, add the following dependency to the _dependencies_ section of your build descriptor:

* Maven (in your `pom.xml`):

```
<dependency>
  <groupId>com.julienviet</groupId>
  <artifactId>vertx-pg-client</artifactId>
  <version>0.3.0</version>
</dependency>
```

* Gradle (in your `build.gradle` file):

```
dependencies {
  compile 'com.julienviet:vertx-pg-client:0.30'
}
```

Use Postgres from Vert.x:

```
PgClient client = PgClient.create(vertx, new PgClientOptions()
  .setPort(5432)
  .setHost("the-host")
  .setDatabase("the-db")
  .setUsername("user")
  .setPassword("secret")
);

client.connect(res -> {
  if (res.succeeded()) {

    // Connected
    PgConnection conn = res.result();

    conn.query("SELECT * FROM USERS", ar -> {

      if (ar.succeeded()) {

        // Use result set
        ResultSet rs = ar.result();
      } else {
        System.out.println("It failed");
      }

      // Close the connection
      conn.close();
    });
  } else {
    System.out.println("Could not connect " + res.cause());
  }
});

```

## Web-site docs

* [Java docs](http://www.julienviet.com/vertx-pg-client/guide/java/index.html)
* [Groovy docs](http://www.julienviet.com/vertx-pg-client/guide/groovy/index.html)
* [Ruby docs](http://www.julienviet.com/vertx-pg-client/guide/ruby/index.html)
* [JavaScript docs](http://www.julienviet.com/vertx-pg-client/guide/js/index.html)

## Snapshots

Snapshots are deploy in Sonatype OSS repository: https://oss.sonatype.org/content/repositories/snapshots/com/julienviet/vertx-pg-client/

## License

Apache License - Version 2.0

## Publishing docs

* mvn package -Pdocs
* cp -r target/docs docs/
* mv docs/vertx-pg-client docs/guide

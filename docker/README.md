## Postgres docker file for testing

Build container with tc installed

```
> docker build -t test/postgres postgres
```

Running the container

```
> docker run --rm --name test-postgres -p 5432:5432 --cap-add=NET_ADMIN test/postgres
```

Add one 1 ms latency to eth0

```
> docker exec -it julien-test tc qdisc add dev eth0 root netem delay 1ms
```

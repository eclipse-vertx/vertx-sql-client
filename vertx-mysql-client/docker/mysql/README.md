## MySQL containers for testing

### Configure the containers

modify the content in the `docker-compose.yml`

### Run the containers

```
> docker-compose up --build -V
```

### TLS testing

The SSL certificate and key files and RSA key-pair files can be generated with [mysql_ssl_rsa_setup](https://dev.mysql.com/doc/refman/8.0/en/mysql-ssl-rsa-setup.html) which is based on OpenSSL and could save a lot of work.

The steps to generate these artifacts are quite easy:

1. make sure you have MySQL installed
2. mysql_ssl_rsa_setup --datadir=./

### Run tests

```
> mvn test -Dconnection.uri=mysql://mysql:password@localhost:3306/testschema -Dtls.connection.uri=mysql://mysql:password@localhost:3307/testschema
```

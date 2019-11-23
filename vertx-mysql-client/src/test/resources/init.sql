# testing change schema
CREATE DATABASE emptyschema;
GRANT ALL ON emptyschema.* TO 'mysql'@'%';

# testing change user
CREATE USER 'superuser'@'%' IDENTIFIED BY 'password';
GRANT ALL ON emptyschema.* TO 'superuser'@'%' WITH GRANT OPTION;

# allow access to information_schema
GRANT PROCESS ON *.* TO 'mysql'@'%';
FLUSH PRIVILEGES;

# testing empty password
CREATE USER 'emptypassuser'@'%' IDENTIFIED BY '';
GRANT ALL ON emptyschema.* TO 'emptypassuser'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

USE testschema;

# datatype testing table
DROP TABLE IF EXISTS datatype;
CREATE TABLE datatype
(
    id             INT NOT NULL PRIMARY KEY,
    `Binary`       BINARY(5),
    `VarBinary`    VARBINARY(20),
    `TinyBlob`     TINYBLOB,
    `Blob`         BLOB,
    `MediumBlob`   MEDIUMBLOB,
    `LongBlob`     LONGBLOB,
    `TinyText`     TINYTEXT,
    `Text`         TEXT,
    `MediumText`   MEDIUMTEXT,
    `LongText`     LONGTEXT,
    `test_enum`    ENUM ('x-small', 'small', 'medium', 'large', 'x-large'),
    `test_set`     SET ('a', 'b', 'c', 'd'),
    test_year      YEAR,
    test_timestamp TIMESTAMP,
    test_datetime  DATETIME(6),
    `test_bit`     BIT(64)
);

INSERT INTO datatype
VALUES (1, 'HELLO', 'HELLO, WORLD', 'TINYBLOB', 'BLOB', 'MEDIUMBLOB', 'LONGBLOB', 'TINYTEXT', 'TEXT', 'MEDIUMTEXT',
        'LONGTEXT', 'small', 'a,b', '2019', '2000-01-01 10:20:30', '2000-01-01 10:20:30.123456', b'11110');
INSERT INTO datatype
VALUES (2, 'hello', 'hello, world', 'tinyblob', 'blob', 'mediumblob', 'longblob', 'tinytext', 'text', 'mediumtext',
        'longtext', 'large', 'b,c,d', '2019', '2000-01-01 10:20:30', '2000-01-01 10:20:30.123456', b'11110');

# TFB tables

# To maintain consistency across servers and fix a problem with the jdbc per
# http://stackoverflow.com/questions/37719818/the-server-time-zone-value-aest-is-unrecognized-or-represents-more-than-one-ti
SET GLOBAL time_zone = '+00:00';

# modified from SO answer http://stackoverflow.com/questions/5125096/for-loop-in-mysql
CREATE DATABASE hello_world;
USE testschema;

-- world table
DROP TABLE IF EXISTS world;
CREATE TABLE world (
  id int(10) unsigned NOT NULL auto_increment,
  randomNumber int NOT NULL default 0,
  PRIMARY KEY  (id)
)
ENGINE=INNODB;

DELIMITER #
CREATE PROCEDURE load_data()
BEGIN

declare v_max int unsigned default 10000;
declare v_counter int unsigned default 0;

  TRUNCATE TABLE world;
  START TRANSACTION;
  while v_counter < v_max do
    INSERT INTO world (randomNumber) VALUES ( floor(0 + (rand() * 10000)) );
    SET v_counter=v_counter+1;
  end while;
  commit;
END #

DELIMITER ;

CALL load_data();

-- Fortune table
DROP TABLE IF EXISTS Fortune;
CREATE TABLE Fortune (
  id int(10) unsigned NOT NULL auto_increment,
  message varchar(2048) CHARACTER SET 'utf8' NOT NULL,
  PRIMARY KEY  (id)
)
ENGINE=INNODB;

INSERT INTO Fortune (message) VALUES ('fortune: No such file or directory');
INSERT INTO Fortune (message) VALUES ('A computer scientist is someone who fixes things that aren''t broken.');
INSERT INTO Fortune (message) VALUES ('After enough decimal places, nobody gives a damn.');
INSERT INTO Fortune (message) VALUES ('A bad random number generator: 1, 1, 1, 1, 1, 4.33e+67, 1, 1, 1');
INSERT INTO Fortune (message) VALUES ('A computer program does what you tell it to do, not what you want it to do.');
INSERT INTO Fortune (message) VALUES ('Emacs is a nice operating system, but I prefer UNIX. — Tom Christaensen');
INSERT INTO Fortune (message) VALUES ('Any program that runs right is obsolete.');
INSERT INTO Fortune (message) VALUES ('A list is only as strong as its weakest link. — Donald Knuth');
INSERT INTO Fortune (message) VALUES ('Feature: A bug with seniority.');
INSERT INTO Fortune (message) VALUES ('Computers make very fast, very accurate mistakes.');
INSERT INTO Fortune (message) VALUES ('<script>alert("This should not be displayed in a browser alert box.");</script>');
INSERT INTO Fortune (message) VALUES ('フレームワークのベンチマーク');

-- local infile test table
-- https://dev.mysql.com/doc/refman/8.0/en/loading-tables.html
DROP TABLE IF EXISTS localinfile;
CREATE TABLE localinfile
(
    name    VARCHAR(20),
    owner   VARCHAR(20),
    species VARCHAR(20),
    sex     CHAR,
    birth   DATE,
    death   DATE
);

-- INSERT INTO localinfile
-- VALUES ('Fluffy', 'Harold', 'cat', 'f', '1993-02-04', NULL);
-- INSERT INTO localinfile
-- VALUES ('Claws', 'Gwen', 'cat', 'm', '1994-03-17', NULL);
-- INSERT INTO localinfile
-- VALUES ('Buffy', 'Harold', 'dog', 'f', '1989-05-13', NULL);
-- INSERT INTO localinfile
-- VALUES ('Fang', 'Benny', 'dog', 'm', '1990-08-27', NULL);
-- INSERT INTO localinfile
-- VALUES ('Bowser', 'Diane', 'dog', 'm', '1979-08-31', '1995-07-29');
-- INSERT INTO localinfile
-- VALUES ('Chirpy', 'Gwen', 'bird', 'f', '1998-09-11', NULL);
-- INSERT INTO localinfile
-- VALUES ('Chirpy', 'Gwen', 'bird', 'f', '1998-09-11', NULL);
-- INSERT INTO localinfile
-- VALUES ('Whistler', 'Gwen', 'bird', NULL, '1997-12-09', NULL);
-- INSERT INTO localinfile
-- VALUES ('Slim', 'Benny', 'snake', 'm', '1996-04-29', NULL);

-- TCK usage --
-- immutable for select query testing --
DROP TABLE IF EXISTS immutable;
CREATE TABLE immutable
(
  id      integer       NOT NULL,
  message varchar(2048) NOT NULL,
  PRIMARY KEY (id)
);

INSERT INTO immutable (id, message)
VALUES (1, 'fortune: No such file or directory');
INSERT INTO immutable (id, message)
VALUES (2, 'A computer scientist is someone who fixes things that aren''t broken.');
INSERT INTO immutable (id, message)
VALUES (3, 'After enough decimal places, nobody gives a damn.');
INSERT INTO immutable (id, message)
VALUES (4, 'A bad random number generator: 1, 1, 1, 1, 1, 4.33e+67, 1, 1, 1');
INSERT INTO immutable (id, message)
VALUES (5, 'A computer program does what you tell it to do, not what you want it to do.');
INSERT INTO immutable (id, message)
VALUES (6, 'Emacs is a nice operating system, but I prefer UNIX. — Tom Christaensen');
INSERT INTO immutable (id, message)
VALUES (7, 'Any program that runs right is obsolete.');
INSERT INTO immutable (id, message)
VALUES (8, 'A list is only as strong as its weakest link. — Donald Knuth');
INSERT INTO immutable (id, message)
VALUES (9, 'Feature: A bug with seniority.');
INSERT INTO immutable (id, message)
VALUES (10, 'Computers make very fast, very accurate mistakes.');
INSERT INTO immutable (id, message)
VALUES (11, '<script>alert("This should not be displayed in a browser alert box.");</script>');
INSERT INTO immutable (id, message)
VALUES (12, 'フレームワークのベンチマーク');
-- immutable for select query testing --

-- mutable for insert,update,delete query testing
DROP TABLE IF EXISTS mutable;
CREATE TABLE mutable
(
  id  integer       NOT NULL,
  val varchar(2048) NOT NULL,
  PRIMARY KEY (id)
);
-- mutable for insert,update,delete query testing

-- table for test ANSI SQL data type codecs
-- MySQL specific so that it works well with TCK, see https://dev.mysql.com/doc/refman/8.0/en/sql-mode.html#sqlmode_ansi
SET sql_mode = 'ANSI';
-- MySQL specific
DROP TABLE IF EXISTS basicdatatype;
CREATE TABLE basicdatatype
(
    id           INTEGER,
    test_int_2   SMALLINT,
    test_int_4   INTEGER,
    test_int_8   BIGINT,
    test_float_4 REAL,
    test_float_8 DOUBLE PRECISION,
    test_numeric NUMERIC(5, 2),
    test_decimal DECIMAL,
    test_boolean BOOLEAN,
    test_char    CHAR(8),
    test_varchar VARCHAR(20),
    test_date    DATE,
    test_time    TIME(6)
);
INSERT INTO basicdatatype(id, test_int_2, test_int_4, test_int_8, test_float_4, test_float_8, test_numeric,
                          test_decimal, test_boolean, test_char, test_varchar, test_date, test_time)
VALUES (1, 32767, 2147483647, 9223372036854775807, 3.40282E38, 1.7976931348623157E308, 999.99,
        12345, TRUE, 'testchar', 'testvarchar', '2019-01-01', '18:45:02');
INSERT INTO basicdatatype(id, test_int_2, test_int_4, test_int_8, test_float_4, test_float_8, test_numeric,
                          test_decimal, test_boolean, test_char, test_varchar, test_date, test_time)
VALUES (2, 32767, 2147483647, 9223372036854775807, 3.40282E38, 1.7976931348623157E308, 999.99,
        12345, TRUE, 'testchar', 'testvarchar', '2019-01-01', '18:45:02');
INSERT INTO basicdatatype(id, test_int_2, test_int_4, test_int_8, test_float_4, test_float_8, test_numeric,
                          test_decimal, test_boolean, test_char, test_varchar, test_date, test_time)
VALUES (3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
-- table for test ANSI SQL data type codecs

-- Collector API testing
DROP TABLE IF EXISTS collector_test;
CREATE TABLE collector_test
(
    id             INT,
    `test_int_2`   SMALLINT,
    `test_int_4`   INT,
    `test_int_8`   BIGINT,
    `test_float`   FLOAT,
    `test_double`  DOUBLE,
    `test_varchar` VARCHAR(20)
);

INSERT INTO collector_test
VALUES (1, 32767, 2147483647, 9223372036854775807, 123.456, 1.234567, 'HELLO,WORLD');
INSERT INTO collector_test
VALUES (2, 32767, 2147483647, 9223372036854775807, 123.456, 1.234567, 'hello,world');

-- TCK usage --
